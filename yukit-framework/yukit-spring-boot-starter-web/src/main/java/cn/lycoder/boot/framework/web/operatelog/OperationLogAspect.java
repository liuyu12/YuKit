package cn.lycoder.boot.framework.web.operatelog;

import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationLogRecord;
import cn.lycoder.boot.framework.common.operatelog.OperationLogRecorder;
import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.web.filter.AccessLogFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 操作日志切面
 * <p>
 * 拦截 {@link OperationLog @OperationLog} 标注的方法，采集请求上下文后组装 {@link OperationLogRecord}，
 * 经 {@link OperationLogRecorder} SPI 交给业务侧落库。
 * <p>
 * 设计要点：
 * <ul>
 *     <li>切面在 DispatcherServlet 之内运行（AOP 作用于 Controller bean），故可安全读取登录态与请求上下文，
 *     规避「最外层 Filter 读不到登录用户」的时机坑；</li>
 *     <li>操作人走 {@link OperatorSupplier} SPI、结果序列化走 Jackson，均以 {@code ObjectProvider} 延迟获取，
 *     缺席即降级，web starter 不反向依赖安全框架/具体存储；</li>
 *     <li>记录过程全程 try/catch 兜底，任何异常只告警，绝不影响业务主流程（fail-open）；</li>
 *     <li>无 {@link OperationLogRecorder} 实现时直接跳过采集，避免无谓开销。</li>
 * </ul>
 */
@Slf4j
@Aspect
@Order(100)
@RequiredArgsConstructor
public class OperationLogAspect {

    /** 入参/结果/异常摘要的最大存储长度，超出截断，防止大对象撑爆日志表 */
    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final int MAX_USER_AGENT_LENGTH = 512;
    /** 敏感字段脱敏：与访问日志一致的凭据/大字段策略 */
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(\"(?:password|oldPassword|newPassword|confirmPassword|code|captcha|token|satoken|imageBase64)\")(\\s*:\\s*)\"[^\"]*\"");
    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP");
    private static final String UNKNOWN = "unknown";

    private final ObjectProvider<OperationLogRecorder> recorderProvider;
    private final ObjectProvider<OperatorSupplier> operatorSupplierProvider;
    private final ObjectProvider<ObjectMapper> objectMapperProvider;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        // 未接持久化实现则不采集，直接放行，避免无谓开销
        OperationLogRecorder recorder = recorderProvider.getIfAvailable();
        if (recorder == null) {
            return joinPoint.proceed();
        }

        LocalDateTime startTime = LocalDateTime.now();
        long startNanos = System.nanoTime();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            try {
                OperationLogRecord record = buildRecord(joinPoint, operationLog,
                        result, error, startTime, (System.nanoTime() - startNanos) / 1_000_000);
                recorder.record(record);
            } catch (Exception e) {
                log.warn("操作日志记录失败: {}", e.getMessage());
            }
        }
    }

    private OperationLogRecord buildRecord(ProceedingJoinPoint joinPoint, OperationLog annotation,
                                           Object result, Throwable error,
                                           LocalDateTime startTime, long durationMs) {
        OperationLogRecord record = new OperationLogRecord();
        record.setTraceId(MDC.get(AccessLogFilter.TRACE_ID_KEY));
        record.setType(annotation.type().getCode());
        record.setModule(annotation.module());
        record.setName(annotation.name());
        record.setOperator(resolveOperator());
        record.setJavaMethod(joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName());
        record.setStartTime(startTime);
        record.setDurationMs(durationMs);

        // 请求上下文（非 Web 线程时可能为空，做好降级）
        HttpServletRequest request = currentRequest();
        if (request != null) {
            record.setRequestMethod(request.getMethod());
            record.setRequestUrl(request.getRequestURI());
            record.setUserIp(resolveClientIp(request));
            record.setUserAgent(truncate(request.getHeader("User-Agent"), MAX_USER_AGENT_LENGTH));
        }

        boolean success = error == null;
        record.setStatus(success ? 0 : 1);
        if (!success) {
            record.setErrorMsg(truncate(error.getClass().getSimpleName() + ": " + error.getMessage(),
                    MAX_CONTENT_LENGTH));
        }

        if (annotation.recordParams()) {
            record.setJavaMethodParam(serializeArgs(joinPoint.getArgs()));
        }
        if (annotation.recordResult() && success) {
            record.setJavaMethodResult(serializeResult(result));
        }
        return record;
    }

    /**
     * 序列化入参：过滤 Servlet/文件/绑定等不可或不宜序列化的类型，逐个转 JSON 后收进数组
     */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<Object> loggable = new ArrayList<>(args.length);
        for (Object arg : args) {
            if (isSkippable(arg)) {
                continue;
            }
            loggable.add(arg);
        }
        return toJson(loggable);
    }

    private String serializeResult(Object result) {
        return result == null ? null : toJson(result);
    }

    /**
     * 用 Jackson 序列化并脱敏、截断；无 ObjectMapper 或序列化失败时降级为 toString，永不抛出
     */
    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        String json;
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
        if (objectMapper != null) {
            try {
                json = objectMapper.writeValueAsString(value);
            } catch (Exception e) {
                json = String.valueOf(value);
            }
        } else {
            json = String.valueOf(value);
        }
        json = json.replaceAll("[\\r\\n\\t]", " ");
        json = SENSITIVE_PATTERN.matcher(json).replaceAll("$1$2\"***\"");
        return truncate(json, MAX_CONTENT_LENGTH);
    }

    private boolean isSkippable(Object arg) {
        if (arg == null) {
            // 保留 null 元素意义不大，且无法判断类型，直接跳过简化输出
            return true;
        }
        String name = arg.getClass().getName();
        return name.startsWith("jakarta.servlet.")
                || name.startsWith("javax.servlet.")
                || name.startsWith("org.springframework.web.multipart.")
                || name.startsWith("org.springframework.validation.")
                || name.startsWith("org.springframework.ui.")
                || name.startsWith("org.springframework.http.")
                || arg instanceof java.io.InputStream
                || arg instanceof java.io.OutputStream;
    }

    /**
     * 通过 OperatorSupplier SPI 获取当前操作人，缺席或无上下文返回 null
     */
    private String resolveOperator() {
        OperatorSupplier supplier = operatorSupplierProvider.getIfAvailable();
        if (supplier == null) {
            return null;
        }
        try {
            String operator = supplier.get();
            return operator == null || operator.isBlank() ? null : operator;
        } catch (Exception e) {
            return null;
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !UNKNOWN.equalsIgnoreCase(ip)) {
                int comma = ip.indexOf(',');
                return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max) + "...(truncated)";
    }
}

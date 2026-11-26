package cn.lycoder.boot.framework.web.filter;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.web.config.YukitAccessLogProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.util.WebUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 接口访问日志拦截器
 * <p>
 * 在 {@link #afterCompletion} 输出单行访问日志。此阶段 DispatcherServlet 尚未清理 Spring 请求上下文，
 * 因此可安全通过 {@link OperatorSupplier} 读取登录用户；业务 code/msg 从 {@link AccessLogFilter} 包装的响应头部解析。
 */
@Slf4j
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final Pattern CODE_PATTERN = Pattern.compile("\"code\"\\s*:\\s*(-?\\d+)");
    private static final Pattern MSG_PATTERN = Pattern.compile("\"msg\"\\s*:\\s*\"(.*?)\"");
    /**
     * 敏感/噪声字段脱敏：键为 password/code/captcha/token 等凭据类及 imageBase64 类大字段，
     * 值为字符串时替换为 ***（数字型业务 code 不受影响）
     */
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(\"(?:password|oldPassword|newPassword|confirmPassword|code|captcha|token|satoken|imageBase64)\")(\\s*:\\s*)\"[^\"]*\"");
    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP");
    private static final String UNKNOWN = "unknown";

    private final YukitAccessLogProperties properties;
    private final ObjectProvider<OperatorSupplier> operatorSupplierProvider;

    public AccessLogInterceptor(YukitAccessLogProperties properties, ObjectProvider<OperatorSupplier> operatorSupplierProvider) {
        this.properties = properties;
        this.operatorSupplierProvider = operatorSupplierProvider;
    }

    /**
     * 空安全标注跟 Spring 侧契约一致（javap 实测 HandlerInterceptor 的 TYPE 注解）：
     * {@code org.springframework.web.servlet} 包已打上 @NullMarked，前三个形参默认非空；
     * 只有 {@code ex} 被 Spring 显式标了 @Nullable（无异常时为 null），不得误标成 @NonNull
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, @Nullable Exception ex) {
        try {
            Object startNanos = request.getAttribute(AccessLogFilter.ATTR_START_NANOS);
            if (!(startNanos instanceof Long start)) {
                // 过滤器未介入（如命中排除路径），跳过
                return;
            }
            long costMs = (System.nanoTime() - start) / 1_000_000;

            String code = "-";
            String msg = "-";
            AccessLogResponseWrapper wrapper =
                    WebUtils.getNativeResponse(response, AccessLogResponseWrapper.class);
            if (wrapper != null && wrapper.getContentHead().length > 0) {
                String body = new String(wrapper.getContentHead(), StandardCharsets.UTF_8);
                Matcher codeMatcher = CODE_PATTERN.matcher(body);
                if (codeMatcher.find()) {
                    code = codeMatcher.group(1);
                }
                Matcher msgMatcher = MSG_PATTERN.matcher(body);
                if (msgMatcher.find()) {
                    msg = msgMatcher.group(1);
                }
            }

            // 主摘要行用占位符格式化；body 单独成条日志输出，通过同一 traceId 关联
            log.info("访问日志 | traceId={} | {} {} | status={} code={} msg={} | cost={}ms | ip={} | user={} | ua={}",
                    MDC.get(AccessLogFilter.TRACE_ID_KEY),
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    code,
                    msg,
                    costMs,
                    resolveClientIp(request),
                    resolveOperator(),
                    truncate(request.getHeader("User-Agent"), 80));
            logBodyInfo(request, wrapper);
        } catch (Exception e) {
            log.warn("访问日志输出异常: {}", e.getMessage());
        }
    }

    /**
     * 按两级开关（全局配置 || 单请求调试头）输出请求/响应体，仅 JSON 且已窥探到内容时打印；
     * 每条 body 独立成一条日志，不与主摘要行连排；变量全部走 {} 占位符参数传入，
     * body 中的 {} 不会被当作占位符解析
     */
    private void logBodyInfo(HttpServletRequest request, AccessLogResponseWrapper wrapper) {
        boolean force = Boolean.TRUE.equals(request.getAttribute(AccessLogFilter.ATTR_FORCE_BODY_LOG));
        String traceId = MDC.get(AccessLogFilter.TRACE_ID_KEY);
        if (properties.isLogRequestBody() || force) {
            AccessLogRequestWrapper requestWrapper =
                    WebUtils.getNativeRequest(request, AccessLogRequestWrapper.class);
            if (requestWrapper != null && requestWrapper.getContentHead().length > 0) {
                log.info("请求体 | traceId={} | {} {} | {}", traceId,
                        request.getMethod(), request.getRequestURI(),
                        formatBody(requestWrapper.getContentHead(), requestWrapper.isTruncated()));
            }
        }
        if ((properties.isLogResponseBody() || force)
                && wrapper != null && wrapper.getContentHead().length > 0 && isJson(wrapper.getContentType())) {
            log.info("响应体 | traceId={} | {} {} | {}", traceId,
                    request.getMethod(), request.getRequestURI(),
                    formatBody(wrapper.getContentHead(), wrapper.isTruncated()));
        }
    }

    private static String formatBody(byte[] head, boolean truncated) {
        // 控制字符压成单空格，避免日志被换行/ANSI 污染；脱敏后输出
        String body = new String(head, StandardCharsets.UTF_8)
                .replaceAll("[\\r\\n\\t]", " ");
        body = SENSITIVE_PATTERN.matcher(body).replaceAll("$1$2\"***\"");
        return truncated ? body + "...(truncated)" : body;
    }

    private static boolean isJson(String contentType) {
        return contentType != null && contentType.toLowerCase().contains("json");
    }

    /**
     * 通过 OperatorSupplier SPI 获取当前登录用户，安全模块缺席或无上下文时返回 "-"
     */
    private String resolveOperator() {
        OperatorSupplier supplier = operatorSupplierProvider.getIfAvailable();
        if (supplier == null) {
            return "-";
        }
        try {
            String operator = supplier.get();
            return operator == null || operator.isBlank() ? "-" : operator;
        } catch (Exception e) {
            return "-";
        }
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
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }
}

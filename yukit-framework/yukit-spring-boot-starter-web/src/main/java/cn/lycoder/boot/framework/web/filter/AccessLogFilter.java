package cn.lycoder.boot.framework.web.filter;

import cn.lycoder.boot.framework.web.config.YukitAccessLogProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 接口访问日志过滤器（职责精简）
 * <p>
 * 仅负责：写入 MDC traceId、回写 {@code X-Trace-Id} 响应头、用 {@link AccessLogResponseWrapper} 有界包装响应、
 * 记录请求起始时间。真正的访问日志摘要由 {@link AccessLogInterceptor} 在 afterCompletion 阶段输出——
 * 那时 Spring 请求上下文仍在，可通过 OperatorSupplier 取到登录用户。
 */
public class AccessLogFilter extends OncePerRequestFilter {

    /**
     * MDC 中 traceId 的 key，与 logback pattern 的 %X{traceId} 对应
     */
    public static final String TRACE_ID_KEY = "traceId";

    /**
     * 回写响应头的 traceId 名称，便于前端/网关串联
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * 单请求 body 调试头：值为 true 且 allow-debug-header 开启时，本次请求强制打印入参/结果 body
     */
    public static final String DEBUG_BODY_HEADER = "X-Access-Log-Body";

    /**
     * 请求属性：本次请求是否强制打印 body（由过滤器解析调试头后写入，拦截器消费）
     */
    public static final String ATTR_FORCE_BODY_LOG = "yukit.accessLog.forceBodyLog";

    /**
     * 请求属性：起始时间（纳秒），由本过滤器写入、拦截器消费
     */
    public static final String ATTR_START_NANOS = "yukit.accessLog.startNanos";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final YukitAccessLogProperties properties;

    public AccessLogFilter(YukitAccessLogProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String pattern : properties.getExcludePaths()) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String traceId = MDC.get(TRACE_ID_KEY);
        boolean transientTrace = traceId == null;
        if (transientTrace) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            MDC.put(TRACE_ID_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
        }
        request.setAttribute(ATTR_START_NANOS, System.nanoTime());
        // 解析单请求调试头，命中标记后由拦截器消费；包装/打印仅对 application/json 生效
        request.setAttribute(ATTR_FORCE_BODY_LOG, isDebugBodyRequested(request));
        HttpServletRequest requestToUse = request;
        if (shouldCaptureRequestBody(request)) {
            requestToUse = new AccessLogRequestWrapper(request, properties.getMaxRequestBodyBytes());
        }
        AccessLogResponseWrapper wrapped =
                new AccessLogResponseWrapper(response, properties.getMaxResponseBodyBytes());
        try {
            chain.doFilter(requestToUse, wrapped);
        } finally {
            if (transientTrace) {
                MDC.remove(TRACE_ID_KEY);
            }
        }
    }

    /**
     * 仅当本次请求可能需要打印入参时包装：全局开关或调试头命中，且为 JSON 内容体
     */
    private boolean shouldCaptureRequestBody(HttpServletRequest request) {
        boolean wanted = properties.isLogRequestBody() || isDebugBodyRequested(request);
        return wanted && isJson(request.getContentType());
    }

    private boolean isDebugBodyRequested(HttpServletRequest request) {
        if (!properties.isAllowDebugHeader()) {
            return false;
        }
        String value = request.getHeader(DEBUG_BODY_HEADER);
        return value != null && "true".equalsIgnoreCase(value.trim());
    }

    private static boolean isJson(String contentType) {
        return contentType != null
                && MediaType.parseMediaType(contentType).isCompatibleWith(MediaType.APPLICATION_JSON);
    }
}

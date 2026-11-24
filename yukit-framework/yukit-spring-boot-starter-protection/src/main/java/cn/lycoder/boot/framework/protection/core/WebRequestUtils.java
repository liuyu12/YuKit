package cn.lycoder.boot.framework.protection.core;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * 保护类切面公共工具：请求上下文解析、客户端 IP、操作人、入参指纹
 * <p>
 * 与访问日志一致：切面在 DispatcherServlet 之内运行，可安全读取请求上下文与登录态。
 */
public final class WebRequestUtils {

    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP");
    private static final String UNKNOWN = "unknown";

    private WebRequestUtils() {
    }

    /**
     * 当前请求，非 Web 线程返回 null
     */
    public static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    public static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !UNKNOWN.equalsIgnoreCase(ip)) {
                int comma = ip.indexOf(',');
                return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 操作人标识，无安全上下文/未登录时回退为客户端 IP，保证键仍有区分度
     */
    public static String resolveOperator(OperatorSupplier supplier, HttpServletRequest request) {
        if (supplier != null) {
            try {
                String operator = supplier.get();
                if (operator != null && !operator.isBlank()) {
                    return operator;
                }
            } catch (Exception ignored) {
                // 未登录或上下文缺失，降级为 IP
            }
        }
        return resolveClientIp(request);
    }

    /**
     * 入参指纹：SHA-256(Arrays.toString(args))，用于区分同地址不同参数的调用。
     * 仅为压缩定长键（非安全用途，但避开 MD5 弱哈希告警）；Controller 方法入参已是绑定好的 DTO，
     * 短窗口去重足够，不含 Servlet 等对象的稳定内容。
     */
    public static String fingerprint(Object[] args) {
        String raw = args == null ? "" : java.util.Arrays.toString(args);
        return sha256Hex(raw);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JDK 规范要求必须支持的算法，正常不会走到
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}

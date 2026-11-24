package cn.lycoder.boot.framework.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口访问日志配置
 * <p>
 * 对应前缀 {@code yukit.access-log}，由 {@link cn.lycoder.boot.framework.web.filter.AccessLogFilter} 消费。
 */
@ConfigurationProperties(prefix = "yukit.access-log")
@Data
public class YukitAccessLogProperties {

    /**
     * 总开关，false 则不装配访问日志过滤器
     */
    private boolean enabled = true;

    /**
     * 不记录访问日志的路径（Ant 风格），默认放行接口文档、健康检查等噪音路径
     */
    private List<String> excludePaths = new ArrayList<>(List.of(
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/actuator/**",
            "/favicon.ico"
    ));

    /**
     * 解析业务 code/msg 时最多窥探的响应体字节数（有界，避免大 body 全量缓冲）
     */
    private int maxResponseBodyBytes = 8192;

    /**
     * 窥探请求体的字节上限（仅 application/json 请求才包装，multipart/表单不读）
     */
    private int maxRequestBodyBytes = 8192;

    /**
     * 全局打印请求体（脱敏+截断），建议只在 dev 开启；单请求调试用 X-Access-Log-Body 头按需触发
     */
    private boolean logRequestBody = false;

    /**
     * 全局打印响应体（脱敏+截断），同上
     */
    private boolean logResponseBody = false;

    /**
     * 是否允许请求头 X-Access-Log-Body: true 对单次请求强制打印 body；生产建议置 false 封死动态开启
     */
    private boolean allowDebugHeader = true;
}

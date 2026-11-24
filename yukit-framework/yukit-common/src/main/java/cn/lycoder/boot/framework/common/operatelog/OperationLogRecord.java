package cn.lycoder.boot.framework.common.operatelog;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志记录模型
 * <p>
 * 切面采集完请求上下文后填充本对象，经 {@link OperationLogRecorder} 传递给业务侧落库。
 * 定义在 common 中作为跨模块的传输契约，使 web starter 与业务持久层互不依赖。
 * <p>
 * 注意：{@code operator} 通过 {@code OperatorSupplier} SPI 取得（用户名/ID 字符串），
 * web starter 因此无需依赖任何具体安全框架。
 */
@Data
public class OperationLogRecord implements Serializable {

    /**
     * 链路追踪 ID，与访问日志同源（MDC traceId），便于按请求串联
     */
    private String traceId;

    /**
     * 操作人标识（用户名 / 用户 ID），无登录态时为 null
     */
    private String operator;

    /**
     * 操作类型码，取值见 {@link OperationType#getCode()}
     */
    private Integer type;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 操作名
     */
    private String name;

    /**
     * HTTP 请求方法（GET/POST/PUT/DELETE）
     */
    private String requestMethod;

    /**
     * 请求 URI
     */
    private String requestUrl;

    /**
     * 操作的 Java 方法，形如 {@code com.x.YController.createY}
     */
    private String javaMethod;

    /**
     * 客户端 IP
     */
    private String userIp;

    /**
     * User-Agent（截断）
     */
    private String userAgent;

    /**
     * 请求入参 JSON（已脱敏、截断）
     */
    private String javaMethodParam;

    /**
     * 响应结果 JSON（已脱敏、截断）
     */
    private String javaMethodResult;

    /**
     * 操作结果状态：0 成功，1 失败
     */
    private Integer status;

    /**
     * 失败时的异常摘要
     */
    private String errorMsg;

    /**
     * 操作开始时间
     */
    private LocalDateTime startTime;

    /**
     * 耗时（毫秒）
     */
    private Long durationMs;
}

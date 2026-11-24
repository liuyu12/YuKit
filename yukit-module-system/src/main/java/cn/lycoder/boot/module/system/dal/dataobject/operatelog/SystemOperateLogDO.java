package cn.lycoder.boot.module.system.dal.dataobject.operatelog;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志 DO，对应表 sys_operate_log
 * <p>
 * 作为一次操作的不可变快照，字段由 {@code OperationLogAspect} 采集、{@code OperateLogRecorderImpl} 落库，
 * 不参与逻辑删除/审计自动填充，故不继承 BaseEntity（记录本身即含操作人与时间）。
 */
@Data
@TableName("sys_operate_log")
public class SystemOperateLogDO implements Serializable {

    /**
     * 操作结果状态 - 成功
     */
    public static final int STATUS_SUCCESS = 0;
    /**
     * 操作结果状态 - 失败
     */
    public static final int STATUS_FAILURE = 1;

    /**
     * 日志主键
     */
    @TableId
    private Long id;

    /**
     * 链路追踪 ID
     */
    private String traceId;

    /**
     * 操作人标识（用户名 / 用户 ID）
     */
    private String operator;

    /**
     * 操作类型码，见 OperationType
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
     * HTTP 请求方法
     */
    private String requestMethod;

    /**
     * 请求 URI
     */
    private String requestUrl;

    /**
     * 操作的 Java 方法
     */
    private String javaMethod;

    /**
     * 客户端 IP
     */
    private String userIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 请求入参 JSON（脱敏、截断）
     */
    private String javaMethodParam;

    /**
     * 响应结果 JSON（脱敏、截断）
     */
    private String javaMethodResult;

    /**
     * 操作结果：0 成功，1 失败
     */
    private Integer status;

    /**
     * 错误信息
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

    /**
     * 落库时间
     */
    private LocalDateTime createTime;
}

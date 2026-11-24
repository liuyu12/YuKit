package cn.lycoder.boot.module.system.controller.admin.operatelog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志 Response VO
 */
@Data
public class OperateLogRespVO implements Serializable {

    @Schema(description = "日志主键")
    private Long id;

    @Schema(description = "链路追踪 ID")
    private String traceId;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "操作类型码")
    private Integer type;

    @Schema(description = "操作类型描述")
    private String typeDescription;

    @Schema(description = "所属模块")
    private String module;

    @Schema(description = "操作名")
    private String name;

    @Schema(description = "HTTP 请求方法")
    private String requestMethod;

    @Schema(description = "请求 URI")
    private String requestUrl;

    @Schema(description = "操作的 Java 方法")
    private String javaMethod;

    @Schema(description = "客户端 IP")
    private String userIp;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "请求入参 JSON")
    private String javaMethodParam;

    @Schema(description = "响应结果 JSON")
    private String javaMethodResult;

    @Schema(description = "操作结果：0 成功，1 失败")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "操作开始时间")
    private LocalDateTime startTime;

    @Schema(description = "耗时（毫秒）")
    private Long durationMs;

    @Schema(description = "落库时间")
    private LocalDateTime createTime;
}

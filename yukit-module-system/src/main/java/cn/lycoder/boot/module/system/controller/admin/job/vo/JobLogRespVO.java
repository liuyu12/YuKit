package cn.lycoder.boot.module.system.controller.admin.job.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务执行日志 Response VO
 */
@Data
@Schema(description = "管理后台 - 定时任务执行日志 Response VO")
public class JobLogRespVO implements Serializable {

    @Schema(description = "日志ID", example = "1")
    private Long jobLogId;

    @Schema(description = "任务ID", example = "1")
    private Long jobId;

    @Schema(description = "任务名称", example = "清理过期登录日志")
    private String jobName;

    @Schema(description = "任务组名", example = "DEFAULT")
    private String jobGroup;

    @Schema(description = "调用目标", example = "loginLogCleanupTask.cleanup")
    private String invokeTarget;

    @Schema(description = "日志摘要", example = "Total: 1 ms")
    private String jobMessage;

    @Schema(description = "执行状态：0 成功 / 1 失败", example = "0")
    private String status;

    @Schema(description = "异常信息")
    private String exceptionInfo;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "耗时（毫秒）", example = "12")
    private Long durationMs;

    @Schema(description = "记录时间")
    private LocalDateTime createTime;
}

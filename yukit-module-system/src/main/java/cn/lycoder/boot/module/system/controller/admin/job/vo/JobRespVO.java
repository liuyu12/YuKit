package cn.lycoder.boot.module.system.controller.admin.job.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务 Response VO
 */
@Data
@Schema(description = "管理后台 - 定时任务 Response VO")
public class JobRespVO implements Serializable {

    @Schema(description = "任务ID", example = "1")
    private Long jobId;

    @Schema(description = "任务名称", example = "清理过期登录日志")
    private String jobName;

    @Schema(description = "任务组名", example = "DEFAULT")
    private String jobGroup;

    @Schema(description = "调用目标", example = "loginLogCleanupTask.cleanup")
    private String invokeTarget;

    @Schema(description = "cron 表达式", example = "0 0 3 * * ?")
    private String cronExpression;

    @Schema(description = "Misfire 策略", example = "3")
    private String misfirePolicy;

    @Schema(description = "是否允许并发：0 允许 / 1 禁止", example = "1")
    private String concurrent;

    @Schema(description = "状态：0 正常 / 1 暂停", example = "0")
    private String status;

    @Schema(description = "下次触发时间（基于 cron 估算，调度器维护）", example = "2026-11-25 03:00:00")
    private LocalDateTime nextFireTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

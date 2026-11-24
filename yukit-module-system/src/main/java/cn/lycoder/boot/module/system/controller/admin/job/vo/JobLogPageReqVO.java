package cn.lycoder.boot.module.system.controller.admin.job.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 定时任务执行日志分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 定时任务执行日志分页查询 Request VO")
public class JobLogPageReqVO extends PageParam {

    @Schema(description = "任务ID", example = "1")
    private Long jobId;

    @Schema(description = "任务名称，模糊匹配", example = "清理")
    private String jobName;

    @Schema(description = "任务组名，精确匹配", example = "DEFAULT")
    private String jobGroup;

    @Schema(description = "执行状态：0 成功 / 1 失败", example = "0")
    private String status;

    @Schema(description = "开始时间范围-起")
    private LocalDateTime beginTime;

    @Schema(description = "开始时间范围-止")
    private LocalDateTime endTime;
}

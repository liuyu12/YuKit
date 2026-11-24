package cn.lycoder.boot.module.system.controller.admin.job.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 定时任务分页查询 Request VO")
public class JobPageReqVO extends PageParam {

    @Schema(description = "任务名称，模糊匹配", example = "清理")
    private String jobName;

    @Schema(description = "任务组名，精确匹配", example = "DEFAULT")
    private String jobGroup;

    @Schema(description = "调用目标，模糊匹配", example = "loginLogCleanupTask")
    private String invokeTarget;

    @Schema(description = "状态：0 正常 / 1 暂停", example = "0")
    private String status;
}

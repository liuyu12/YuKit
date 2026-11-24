package cn.lycoder.boot.module.system.controller.admin.job.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 定时任务创建 Request VO
 */
@Data
@Schema(description = "管理后台 - 定时任务创建 Request VO")
public class JobCreateReqVO {

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "清理过期登录日志")
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 64, message = "任务名称长度不能超过 64")
    private String jobName;

    @Schema(description = "任务组名", requiredMode = Schema.RequiredMode.REQUIRED, example = "DEFAULT")
    @NotBlank(message = "任务组名不能为空")
    @Size(max = 64, message = "任务组名长度不能超过 64")
    private String jobGroup;

    @Schema(description = "调用目标字符串：beanName.methodName(arg1,arg2)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "loginLogCleanupTask.cleanup")
    @NotBlank(message = "调用目标字符串不能为空")
    @Size(max = 500, message = "调用目标长度不能超过 500")
    @Pattern(regexp = "^[A-Za-z0-9_\\-.]+\\.[A-Za-z0-9_]+(\\(.*\\))?$",
            message = "调用目标格式须为 beanName.methodName 或 beanName.methodName(args)")
    private String invokeTarget;

    @Schema(description = "cron 表达式（Spring 格式：秒 分 时 日 月 周 [年]）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "0 0 3 * * ?")
    @NotBlank(message = "cron 表达式不能为空")
    @Size(max = 255, message = "cron 表达式长度不能超过 255")
    private String cronExpression;

    @Schema(description = "Misfire 策略：1 立即执行 / 2 执行一次 / 3 放弃执行", example = "3")
    @Pattern(regexp = "^[123]$", message = "Misfire 策略仅支持 1/2/3")
    private String misfirePolicy = "3";

    @Schema(description = "是否允许并发：0 允许 / 1 禁止", example = "1")
    @Pattern(regexp = "^[01]$", message = "并发策略仅支持 0/1")
    private String concurrent = "1";

    @Schema(description = "状态：0 正常 / 1 暂停", example = "0")
    @Pattern(regexp = "^[01]$", message = "状态仅支持 0/1")
    private String status = "0";

    @Schema(description = "备注", example = "每日凌晨清理 30 天前的登录日志")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;
}

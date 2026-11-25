package cn.lycoder.boot.module.system.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 参数创建 Request VO
 */
@Data
@Schema(description = "管理后台 - 参数创建 Request VO")
public class ConfigCreateReqVO {

    @NotBlank(message = "参数名称不能为空")
    @Size(max = 100, message = "参数名称长度不能超过 100")
    @Schema(description = "参数名称", example = "账号初始密码")
    private String configName;

    @NotBlank(message = "参数键名不能为空")
    @Size(max = 100, message = "参数键名长度不能超过 100")
    @Pattern(regexp = "^[\\w.-]+$", message = "参数键名只能包含字母、数字、下划线、点和中划线")
    @Schema(description = "参数键名（全局唯一）", example = "sys.user.initPassword")
    private String configKey;

    @NotBlank(message = "参数键值不能为空")
    @Size(max = 500, message = "参数键值长度不能超过 500")
    @Schema(description = "参数键值", example = "123456")
    private String configValue;

    @Schema(description = "是否内置：Y 是 / N 否，为空默认 N", example = "N")
    private String configType;

    @Size(max = 500, message = "备注长度不能超过 500")
    @Schema(description = "备注", example = "重置密码功能使用的默认密码")
    private String remark;
}

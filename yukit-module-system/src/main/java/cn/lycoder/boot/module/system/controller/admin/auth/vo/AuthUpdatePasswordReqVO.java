package cn.lycoder.boot.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 账号自助修改密码 Request VO
 */
@Data
@Schema(description = "管理后台 - 账号自助修改密码 Request VO")
public class AuthUpdatePasswordReqVO {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码（明文）", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin123")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 30, message = "新密码长度需在6-30个字符之间")
    @Schema(description = "新密码（明文，与创建用户密码约束一致）", requiredMode = Schema.RequiredMode.REQUIRED, example = "newPass123")
    private String newPassword;
}

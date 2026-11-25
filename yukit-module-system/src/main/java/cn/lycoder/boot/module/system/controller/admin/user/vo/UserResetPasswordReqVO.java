package cn.lycoder.boot.module.system.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户重置密码 Request VO
 * <p>
 * password 留空（不传或空串）时取系统参数 sys.user.initPassword（参数缺失回退 123456）；
 * 指定时长度须 6-30，与创建用户密码约束一致
 */
@Data
@Schema(description = "管理后台 - 用户重置密码 Request VO")
public class UserResetPasswordReqVO {

    @Pattern(regexp = "^(|[\\x21-~]{6,30})$", message = "新密码长度需在6-30个字符之间且不含空白字符")
    @Schema(description = "新密码（明文，可选；留空则使用参数 sys.user.initPassword）", example = "newPass123")
    private String password;
}

package cn.lycoder.boot.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class AuthLoginReqVO {

    /**
     * 用户账号
     */
    @Schema(description = "用户账号", example = "admin")
    @NotBlank(message = "用户账号不能为空")
    @Size(max = 30, message = "用户账号长度不能超过30个字符")
    private String username;

    /**
     * 用户密码（明文，服务端与 BCrypt 存储值比对）
     */
    @Schema(description = "用户密码（明文，服务端与 BCrypt 存储值比对）", example = "admin123")
    @NotBlank(message = "用户密码不能为空")
    @Size(max = 100, message = "用户密码长度不能超过100个字符")
    private String password;

    /**
     * 验证码唯一标识（取自 /system/auth/captcha 返回的 uuid）
     * <p>
     * 不加 @NotBlank：当 {@code yukit.captcha.enabled=false} 时前端可不传，
     * 必填性由服务端根据开关在 CaptchaService 中判定
     */
    @Schema(description = "验证码唯一标识，取自 /system/auth/captcha 返回的 uuid", example = "6ac11e7c0d27494882afe548895cc945")
    private String uuid;

    /**
     * 用户输入的验证码值
     */
    @Schema(description = "用户输入的验证码值", example = "Hk6c")
    private String code;
}

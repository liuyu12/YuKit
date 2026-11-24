package cn.lycoder.boot.module.system.enums.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录结果枚举（sys_login_log.result）
 * <p>
 * 与错误码语义对齐：既记录成功，也区分失败原因，供安全审计与风控统计
 */
@Getter
@AllArgsConstructor
public enum LoginResultTypeEnum {

    SUCCESS(0, "登录成功"),
    BAD_CREDENTIALS(1, "账号或密码错误"),
    USER_DISABLED(2, "账号被禁用"),
    ACCOUNT_LOCKED(3, "账号被锁定"),
    CAPTCHA_ERROR(4, "验证码校验失败"),
    SYSTEM_ERROR(5, "系统异常");

    /**
     * 结果码
     */
    private final int code;
    /**
     * 结果描述
     */
    private final String description;
}

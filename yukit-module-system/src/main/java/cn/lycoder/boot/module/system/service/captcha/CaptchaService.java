package cn.lycoder.boot.module.system.service.captcha;

import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthCaptchaRespVO;

/**
 * 登录验证码 Service
 */
public interface CaptchaService {

    /**
     * 生成图形验证码：图片 base64 + uuid，答案以 uuid 为键存入 Redis 并设置过期时间
     *
     * @return 验证码响应
     */
    AuthCaptchaRespVO generate();

    /**
     * 校验验证码：一次性使用，校验后无论成败均删除，防重放与暴力枚举。
     * <p>
     * 当 {@code yukit.captcha.enabled=false} 时直接跳过校验。
     *
     * @param uuid 验证码标识
     * @param code 用户输入的验证码值
     */
    void validate(String uuid, String code);
}

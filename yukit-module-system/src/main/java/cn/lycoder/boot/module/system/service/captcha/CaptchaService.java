package cn.lycoder.boot.module.system.service.captcha;

import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthCaptchaRespVO;

/**
 * 登录验证码 Service
 */
public interface CaptchaService {

    /**
     * 验证码校验是否启用：部署级总闸 {@code yukit.captcha.enabled} AND
     * 运行期参数 {@code sys.account.captchaEnabled}（参数缺失时默认启用）。
     * 运营可在「参数管理」改值即时生效，无需重启。
     *
     * @return true 表示登录必须通过验证码校验
     */
    boolean isEnabled();

    /**
     * 生成图形验证码：图片 base64 + uuid，答案以 uuid 为键存入 Redis 并设置过期时间。
     * <p>
     * 开关关闭时不生成图片与 Redis 写入，仅回填 captchaEnabled=false。
     *
     * @return 验证码响应（含开关状态，供登录页条件渲染）
     */
    AuthCaptchaRespVO generate();

    /**
     * 校验验证码：一次性使用，校验后无论成败均删除，防重放与暴力枚举。
     * <p>
     * {@link #isEnabled()} 为 false（总闸或参数任一关闭）时直接跳过校验。
     *
     * @param uuid 验证码标识
     * @param code 用户输入的验证码值
     */
    void validate(String uuid, String code);
}

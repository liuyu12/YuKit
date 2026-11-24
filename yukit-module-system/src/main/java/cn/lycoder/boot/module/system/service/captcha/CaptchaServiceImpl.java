package cn.lycoder.boot.module.system.service.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.config.CaptchaProperties;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthCaptchaRespVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_LOGIN_CAPTCHA_CODE_ERROR;

/**
 * 登录验证码 Service 实现
 * <p>
 * Hutool {@link LineCaptcha} 生成干扰线字符图，答案以 uuid 为键存 Redis（TTL 可配）。
 * 校验一次性：读取后立即删除，避免同一验证码被重放或用于暴力比对。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate redisTemplate;
    private final CaptchaProperties properties;
    private final Environment environment;

    @Override
    public AuthCaptchaRespVO generate() {
        // 生成验证码图片（干扰线字符码）
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(
                properties.getWidth(), properties.getHeight(),
                properties.getCodeCount(), properties.getLineCount());

        String uuid = IdUtil.fastSimpleUUID();
        String code = captcha.getCode();
        // 存答案，到期自动失效
        redisTemplate.opsForValue().set(key(uuid), code,
                Duration.ofSeconds(properties.getExpireSeconds()));

        // getImageBase64Data() 返回 data:image/png;base64,... 可直接给前端 img 用
        AuthCaptchaRespVO resp = new AuthCaptchaRespVO(uuid, captcha.getImageBase64Data(), null);
        // 开发环境（dev/default，与 logback 的 dev|default 分支保持一致）回填答案，方便接口测试直读；
        // 生产环境 code 保持 null，配合 VO 上的 NON_NULL 不会输出
        if (environment.acceptsProfiles(Profiles.of("dev", "default"))) {
            resp.setCode(code);
        }
        return resp;
    }

    @Override
    public void validate(String uuid, String code) {
        // 关闭开关时跳过（开发/内网调试用）
        if (!properties.isEnabled()) {
            return;
        }
        if (CharSequenceUtil.isBlank(uuid) || CharSequenceUtil.isBlank(code)) {
            throw new ServiceException(AUTH_LOGIN_CAPTCHA_CODE_ERROR.getCode(), "验证码不能为空");
        }

        String key = key(uuid);
        String expected = redisTemplate.opsForValue().get(key);
        // 一次性使用：无论后续成败均删除，强制失败方重新获取验证码
        redisTemplate.delete(key);

        if (expected == null) {
            throw new ServiceException(AUTH_LOGIN_CAPTCHA_CODE_ERROR.getCode(), "验证码已失效，请刷新重试");
        }
        if (!expected.equalsIgnoreCase(code)) {
            throw new ServiceException(AUTH_LOGIN_CAPTCHA_CODE_ERROR.getCode(), "验证码不正确");
        }
    }

    private String key(String uuid) {
        return properties.getRedisKeyPrefix() + uuid;
    }
}

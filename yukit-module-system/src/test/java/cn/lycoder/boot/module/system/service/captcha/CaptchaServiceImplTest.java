package cn.lycoder.boot.module.system.service.captcha;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.config.CaptchaProperties;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthCaptchaRespVO;
import cn.lycoder.boot.module.system.service.config.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * CaptchaServiceImpl 单测：纯 Mockito，不起 Spring 上下文
 * <p>
 * 重点覆盖双层开关判定（总闸 AND 参数）及关闭时零 Redis 交互。
 */
@ExtendWith(MockitoExtension.class)
class CaptchaServiceImplTest {

    private static final String CONFIG_KEY = "sys.account.captchaEnabled";

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private Environment environment;
    @Mock
    private ConfigService configService;

    private CaptchaProperties properties;
    private CaptchaServiceImpl captchaService;

    @BeforeEach
    void setUp() {
        properties = new CaptchaProperties();
        captchaService = new CaptchaServiceImpl(redisTemplate, properties, environment, configService);
    }

    @Test
    void isEnabledShouldBeFalseWhenMasterSwitchOff() {
        properties.setEnabled(false);

        assertThat(captchaService.isEnabled()).isFalse();
        // 总闸短路，不应触达参数查询
        verifyNoInteractions(configService);
    }

    @Test
    void isEnabledShouldBeFalseWhenParamDisabled() {
        properties.setEnabled(true);
        when(configService.getConfigValueAsBoolean(eq(CONFIG_KEY), eq(true))).thenReturn(false);

        assertThat(captchaService.isEnabled()).isFalse();
    }

    @Test
    void isEnabledShouldDefaultTrueWhenParamMissing() {
        properties.setEnabled(true);
        when(configService.getConfigValueAsBoolean(eq(CONFIG_KEY), eq(true))).thenReturn(true);

        assertThat(captchaService.isEnabled()).isTrue();
    }

    @Test
    void validateShouldSkipAndTouchNoRedisWhenDisabled() {
        properties.setEnabled(false);

        captchaService.validate(null, null);

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void validateShouldRejectBlankInputWhenEnabled() {
        properties.setEnabled(true);
        when(configService.getConfigValueAsBoolean(eq(CONFIG_KEY), eq(true))).thenReturn(true);

        assertThatThrownBy(() -> captchaService.validate("uuid-1", " "))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("验证码不能为空");
    }

    @Test
    void validateShouldRejectExpiredAndDeleteKey() {
        properties.setEnabled(true);
        when(configService.getConfigValueAsBoolean(eq(CONFIG_KEY), eq(true))).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("yukit:captcha:uuid-2")).thenReturn(null);

        assertThatThrownBy(() -> captchaService.validate("uuid-2", "ab12"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已失效");
        verify(redisTemplate).delete("yukit:captcha:uuid-2");
    }

    @Test
    void validateShouldRejectWrongCodeCaseInsensitively() {
        properties.setEnabled(true);
        when(configService.getConfigValueAsBoolean(eq(CONFIG_KEY), eq(true))).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("yukit:captcha:uuid-3")).thenReturn("Ab12");

        assertThatThrownBy(() -> captchaService.validate("uuid-3", "zz99"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("验证码不正确");
        verify(redisTemplate).delete("yukit:captcha:uuid-3");
    }

    @Test
    void validateShouldPassOnMatchingCodeAndConsumeOnce() {
        properties.setEnabled(true);
        when(configService.getConfigValueAsBoolean(eq(CONFIG_KEY), eq(true))).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("yukit:captcha:uuid-4")).thenReturn("Ab12");

        captchaService.validate("uuid-4", "ab12");

        // 一次性使用：通过后同样删除
        verify(redisTemplate).delete("yukit:captcha:uuid-4");
    }

    @Test
    void generateShouldReturnDisabledWithoutSideEffects() {
        properties.setEnabled(false);

        AuthCaptchaRespVO resp = captchaService.generate();

        assertThat(resp.getCaptchaEnabled()).isFalse();
        assertThat(resp.getUuid()).isNull();
        assertThat(resp.getImageBase64()).isNull();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void generateShouldProduceUuidImageAndBackfillCodeInDevProfile() {
        properties.setEnabled(true);
        when(configService.getConfigValueAsBoolean(eq(CONFIG_KEY), eq(true))).thenReturn(true);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthCaptchaRespVO resp = captchaService.generate();

        assertThat(resp.getCaptchaEnabled()).isTrue();
        assertThat(resp.getUuid()).hasSize(32);
        assertThat(resp.getImageBase64()).startsWith("data:image/png;base64,");
        // dev/default 环境回填答案，供接口测试直读
        assertThat(resp.getCode()).hasSize(properties.getCodeCount());
        verify(valueOperations).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void generateShouldNotBackfillCodeInNonDevProfile() {
        properties.setEnabled(true);
        when(configService.getConfigValueAsBoolean(eq(CONFIG_KEY), eq(true))).thenReturn(true);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthCaptchaRespVO resp = captchaService.generate();

        assertThat(resp.getCode()).isNull();
    }
}

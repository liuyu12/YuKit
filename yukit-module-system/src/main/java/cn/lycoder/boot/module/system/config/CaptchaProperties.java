package cn.lycoder.boot.module.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录验证码配置项
 * <p>
 * 图形验证码基于 Hutool，答案存 Redis。开发期可 {@code yukit.captcha.enabled=false} 整体关闭。
 */
@Data
@Component
@ConfigurationProperties(prefix = "yukit.captcha")
public class CaptchaProperties {

    /**
     * 是否开启登录验证码校验，默认开启
     */
    private boolean enabled = true;

    /**
     * 验证码有效期（秒），到期未使用则失效需刷新
     */
    private long expireSeconds = 120;

    /**
     * 图片宽度（像素）
     */
    private int width = 120;

    /**
     * 图片高度（像素）
     */
    private int height = 40;

    /**
     * 验证码字符个数
     */
    private int codeCount = 4;

    /**
     * 干扰线数量
     */
    private int lineCount = 20;

    /**
     * Redis 中验证码答案的 key 前缀
     */
    private String redisKeyPrefix = "yukit:captcha:";

}

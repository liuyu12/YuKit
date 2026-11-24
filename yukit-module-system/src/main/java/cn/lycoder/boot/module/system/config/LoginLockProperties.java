package cn.lycoder.boot.module.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录失败锁定配置项
 * <p>
 * 基于 Redis 计数器：同一账号在 {@code windowSeconds} 窗口内连续失败达 {@code maxFailCount} 次即锁定，
 * 窗口到期自动解锁（计数键 TTL 到时自然消失），管理员亦可提前强制解锁。
 */
@Data
@Component
@ConfigurationProperties(prefix = "yukit.login-lock")
public class LoginLockProperties {

    /**
     * 是否开启失败锁定，默认开启
     */
    private boolean enabled = true;

    /**
     * 触发锁定的连续失败次数
     */
    private int maxFailCount = 5;

    /**
     * 失败计数窗口（秒），同时作为锁定时长：最后一次失败后经过该时长自动解锁
     */
    private long windowSeconds = 600;

    /**
     * Redis 失败计数键前缀
     */
    private String failKeyPrefix = "yukit:login:fail:";
}

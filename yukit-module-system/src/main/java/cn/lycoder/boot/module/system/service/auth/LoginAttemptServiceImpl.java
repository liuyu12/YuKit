package cn.lycoder.boot.module.system.service.auth;

import cn.lycoder.boot.module.system.config.LoginLockProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 登录失败计数 / 账号锁定 Service 实现
 * <p>
 * 单键 {@code failKeyPrefix + username} 存连续失败次数，每次失败刷新 TTL={@code windowSeconds}，
 * 因此锁定实际从"最后一次失败"起算并自动到期解锁，无需额外的锁定标记键。
 * 关闭开关时所有判定短路放行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    /** 秒→分钟向上取整的分度基数 */
    private static final long SECONDS_PER_MINUTE = 60L;

    private final StringRedisTemplate redisTemplate;
    private final LoginLockProperties properties;

    @Override
    public boolean isLocked(String username) {
        if (!properties.isEnabled() || username == null) {
            return false;
        }
        return currentFailCount(username) >= properties.getMaxFailCount();
    }

    @Override
    public int recordFailure(String username) {
        if (!properties.isEnabled() || username == null) {
            return 0;
        }
        String key = key(username);
        Long count = redisTemplate.opsForValue().increment(key);
        long latest = count == null ? 0L : count;
        // 每次失败都刷新窗口，锁定从最后一次失败起算
        redisTemplate.expire(key, Duration.ofSeconds(properties.getWindowSeconds()));
        return (int) latest;
    }

    @Override
    public long getLockRemainMinutes(String username) {
        if (!properties.isEnabled() || username == null) {
            return 0;
        }
        Long seconds = redisTemplate.getExpire(key(username));
        // -1=无过期时间，-2=键不存在；均视为无剩余锁定
        if (seconds == null || seconds <= 0) {
            return 0;
        }
        // 向上取整到分钟，至少 1 分钟
        return Math.ceilDiv(seconds, SECONDS_PER_MINUTE);
    }

    @Override
    public void reset(String username) {
        if (username == null) {
            return;
        }
        redisTemplate.delete(key(username));
    }

    private long currentFailCount(String username) {
        String value = redisTemplate.opsForValue().get(key(username));
        return value == null ? 0L : Long.parseLong(value);
    }

    private String key(String username) {
        return properties.getFailKeyPrefix() + username;
    }
}

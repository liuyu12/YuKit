package cn.lycoder.boot.module.system.service.auth;

import cn.lycoder.boot.module.system.config.LoginLockProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LoginAttemptServiceImpl} 失败计数与锁定判定单元测试（纯 Mockito，不依赖真实 Redis）
 */
class LoginAttemptServiceImplTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private LoginLockProperties properties;
    private LoginAttemptServiceImpl service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        properties = new LoginLockProperties();
        properties.setEnabled(true);
        properties.setMaxFailCount(5);
        properties.setWindowSeconds(600);
        properties.setFailKeyPrefix("yukit:login:fail:");
        service = new LoginAttemptServiceImpl(redisTemplate, properties);
    }

    @Test
    @DisplayName("开关关闭：isLocked 恒 false，不触碰 Redis")
    void isLockedDisabled() {
        properties.setEnabled(false);
        assertThat(service.isLocked("ry")).isFalse();
        verify(valueOperations, never()).get(any());
    }

    @Test
    @DisplayName("isLocked：达到阈值锁定，未达放行，键不存在放行")
    void isLockedByCount() {
        when(valueOperations.get("yukit:login:fail:ry")).thenReturn("4");
        assertThat(service.isLocked("ry")).isFalse();

        when(valueOperations.get("yukit:login:fail:ry")).thenReturn("5");
        assertThat(service.isLocked("ry")).isTrue();

        when(valueOperations.get("yukit:login:fail:ry")).thenReturn(null);
        assertThat(service.isLocked("ry")).isFalse();
    }

    @Test
    @DisplayName("recordFailure：INCR 返回累计值并刷新窗口 TTL")
    void recordFailure() {
        when(valueOperations.increment("yukit:login:fail:ry")).thenReturn(3L);
        assertThat(service.recordFailure("ry")).isEqualTo(3);
        verify(redisTemplate).expire(eq("yukit:login:fail:ry"), eq(Duration.ofSeconds(600)));
    }

    @Test
    @DisplayName("getLockRemainMinutes：秒向上取整到分钟，无键/无过期返回 0")
    void lockRemain() {
        when(redisTemplate.getExpire("yukit:login:fail:ry")).thenReturn(600L);
        assertThat(service.getLockRemainMinutes("ry")).isEqualTo(10);

        // 61 秒向上取整 = 2 分钟
        when(redisTemplate.getExpire("yukit:login:fail:ry")).thenReturn(61L);
        assertThat(service.getLockRemainMinutes("ry")).isEqualTo(2);

        // -1 无过期、-2 键不存在、<=0 均视为无剩余
        when(redisTemplate.getExpire("yukit:login:fail:ry")).thenReturn(-1L);
        assertThat(service.getLockRemainMinutes("ry")).isZero();
        when(redisTemplate.getExpire("yukit:login:fail:ry")).thenReturn(-2L);
        assertThat(service.getLockRemainMinutes("ry")).isZero();
    }

    @Test
    @DisplayName("reset：按用户名删除计数键")
    void reset() {
        service.reset("ry");
        verify(redisTemplate).delete("yukit:login:fail:ry");
    }
}

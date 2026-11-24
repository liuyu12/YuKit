package cn.lycoder.boot.framework.protection.ratelimiter;

import cn.lycoder.boot.framework.common.enums.GlobalErrorCodeConstants;
import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RateLimiterAspect} 单元测试：Redis 缺席放行、未超限执行、超限拒绝(429)
 */
class RateLimiterAspectTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<StringRedisTemplate> redisProvider = mock(ObjectProvider.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<OperatorSupplier> operatorProvider = mock(ObjectProvider.class);
    private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    private final RateLimiter rateLimiter = mock(RateLimiter.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);

    private RateLimiterAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new RateLimiterAspect(redisProvider, operatorProvider);
        when(redisProvider.getIfAvailable()).thenReturn(redis);
        when(operatorProvider.getIfAvailable()).thenReturn(() -> "u1");
        when(rateLimiter.count()).thenReturn(5);
        when(rateLimiter.time()).thenReturn(60);
        when(rateLimiter.limitType()).thenReturn(LimitType.DEFAULT);
        when(rateLimiter.message()).thenReturn("请求过于频繁");
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("Demo.login(..)");
        when(joinPoint.getSignature()).thenReturn(signature);
    }

    @Test
    @DisplayName("Redis 缺席：放行")
    void noRedis_shouldProceed() throws Throwable {
        when(redisProvider.getIfAvailable()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("ok");

        assertThat(aspect.around(joinPoint, rateLimiter)).isEqualTo("ok");
    }

    @Test
    @DisplayName("未超限（脚本返回 1）：执行业务")
    void underLimit_shouldProceed() throws Throwable {
        stubExecute(1L);
        when(joinPoint.proceed()).thenReturn("ok");

        assertThat(aspect.around(joinPoint, rateLimiter)).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("超限（脚本返回 0）：抛 TOO_MANY_REQUESTS(429)，业务不执行")
    void overLimit_shouldThrow429() throws Throwable {
        stubExecute(0L);

        assertThatThrownBy(() -> aspect.around(joinPoint, rateLimiter))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(GlobalErrorCodeConstants.TOO_MANY_REQUESTS.getCode());

        verify(joinPoint, never()).proceed();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubExecute(Long result) {
        when(redis.execute(any(RedisScript.class), anyList(), any(), any())).thenReturn(result);
    }
}

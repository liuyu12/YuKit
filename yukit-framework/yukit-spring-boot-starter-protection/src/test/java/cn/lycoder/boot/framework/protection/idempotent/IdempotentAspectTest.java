package cn.lycoder.boot.framework.protection.idempotent;

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
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link IdempotentAspect} 单元测试：Redis 缺席放行、抢占成功执行、重复拒绝、异常释放键
 */
class IdempotentAspectTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<StringRedisTemplate> redisProvider = mock(ObjectProvider.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<OperatorSupplier> operatorProvider = mock(ObjectProvider.class);

    private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    private final Idempotent idempotent = mock(Idempotent.class);

    private IdempotentAspect aspect;
    private StringRedisTemplate redis;
    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> valueOps = mock(ValueOperations.class);

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        aspect = new IdempotentAspect(redisProvider, operatorProvider);
        redis = mock(StringRedisTemplate.class);
        when(redisProvider.getIfAvailable()).thenReturn(redis);
        when(operatorProvider.getIfAvailable()).thenReturn(() -> "u1");
        when(idempotent.timeout()).thenReturn(3);
        when(idempotent.message()).thenReturn("请勿重复提交");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"payload"});
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("Demo.create(..)");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @DisplayName("Redis 缺席：直接放行，不做判重")
    void noRedis_shouldProceed() throws Throwable {
        when(redisProvider.getIfAvailable()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint, idempotent);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("抢占成功：执行业务且不释放键")
    void acquired_shouldProceedAndKeepKey() throws Throwable {
        when(valueOps.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint, idempotent);

        assertThat(result).isEqualTo("ok");
        verify(valueOps).setIfAbsent(anyString(), eq("1"), any(Duration.class));
        verify(redis, never()).delete(anyString());
    }

    @Test
    @DisplayName("抢占失败：抛 REPEATED_REQUESTS(900)，业务不执行")
    void notAcquired_shouldThrow900() throws Throwable {
        when(valueOps.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> aspect.around(joinPoint, idempotent))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(GlobalErrorCodeConstants.REPEATED_REQUESTS.getCode());

        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("业务异常：向上抛出并释放幂等键以允许重试")
    void businessError_shouldReleaseKey() throws Throwable {
        when(valueOps.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> aspect.around(joinPoint, idempotent))
                .isInstanceOf(IllegalStateException.class);

        verify(redis).delete(anyString());
    }
}

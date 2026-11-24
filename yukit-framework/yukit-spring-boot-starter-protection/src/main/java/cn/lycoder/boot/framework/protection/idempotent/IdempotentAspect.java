package cn.lycoder.boot.framework.protection.idempotent;

import cn.lycoder.boot.framework.common.enums.GlobalErrorCodeConstants;
import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.protection.core.WebRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.core.annotation.Order;

import java.time.Duration;

/**
 * 幂等切面
 * <p>
 * 依据「操作人 + 请求 URI + 入参指纹」构造 Redis 键，用 {@code SET NX PX}（{@code setIfAbsent}）抢占：
 * 抢不到即视为窗口内重复提交并拒绝；抢到则执行业务，失败/异常时删除键以允许立即重试，成功则让键自然过期屏蔽重复。
 * <p>
 * Redis 缺席（{@link StringRedisTemplate} 未装配）时直接放行，保证 starter 不因缺少 Redis 而阻断业务。
 */
@Slf4j
@Aspect
@Order(10)
@RequiredArgsConstructor
public class IdempotentAspect {

    private static final String KEY_PREFIX = "yukit:idempotent:";

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ObjectProvider<OperatorSupplier> operatorSupplierProvider;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
        // 无 Redis：无法判重，放行（fail-open）
        if (redis == null) {
            return joinPoint.proceed();
        }

        String key = buildKey(joinPoint);
        Boolean acquired = redis.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(Math.max(1, idempotent.timeout())));
        if (!Boolean.TRUE.equals(acquired)) {
            throw new ServiceException(GlobalErrorCodeConstants.REPEATED_REQUESTS.getCode(), idempotent.message());
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            // 业务失败：释放键，允许用户立即重试
            try {
                redis.delete(key);
            } catch (Exception e) {
                log.warn("幂等键释放失败 key={}: {}", key, e.getMessage());
            }
            throw ex;
        }
    }

    private String buildKey(ProceedingJoinPoint joinPoint) {
        HttpServletRequest request = WebRequestUtils.currentRequest();
        OperatorSupplier supplier = operatorSupplierProvider.getIfAvailable();
        String operator = WebRequestUtils.resolveOperator(supplier, request);
        String uri = request != null ? request.getRequestURI()
                : joinPoint.getSignature().toShortString();
        return KEY_PREFIX + operator + ":" + uri + ":" + WebRequestUtils.fingerprint(joinPoint.getArgs());
    }
}

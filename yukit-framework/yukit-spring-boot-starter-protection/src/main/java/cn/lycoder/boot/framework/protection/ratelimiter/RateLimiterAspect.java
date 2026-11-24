package cn.lycoder.boot.framework.protection.ratelimiter;

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
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.core.annotation.Order;

import java.util.Collections;

/**
 * 限流切面
 * <p>
 * 固定窗口计数：窗口编号（{@code 时间戳 / 窗口毫秒}）编入键，天然滚动换窗；Lua 脚本原子 {@code INCR}
 * 并在首次计数时设置过期，返回是否放行。维度支持操作人 / IP / 全局。Redis 缺席时放行。
 */
@Slf4j
@Aspect
@Order(5)
@RequiredArgsConstructor
public class RateLimiterAspect {

    private static final String KEY_PREFIX = "yukit:rate_limit:";

    /**
     * 原子限流脚本：ARGV[1]=上限，ARGV[2]=窗口秒；返回 1 放行、0 拒绝
     */
    @SuppressWarnings({"rawtypes"})
    private static final RedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local c = redis.call('INCR', KEYS[1]) "
                    + "if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[2]) end "
                    + "if c > tonumber(ARGV[1]) then return 0 else return 1 end",
            Long.class);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ObjectProvider<OperatorSupplier> operatorSupplierProvider;

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
        // 无 Redis：无法限流，放行（fail-open）
        if (redis == null) {
            return joinPoint.proceed();
        }

        String key = buildKey(joinPoint, rateLimiter);
        Long allowed = redis.execute(RATE_LIMIT_SCRIPT, Collections.singletonList(key),
                String.valueOf(rateLimiter.count()), String.valueOf(Math.max(1, rateLimiter.time())));
        if (allowed != null && allowed == 0L) {
            throw new ServiceException(GlobalErrorCodeConstants.TOO_MANY_REQUESTS.getCode(), rateLimiter.message());
        }
        return joinPoint.proceed();
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        HttpServletRequest request = WebRequestUtils.currentRequest();
        String method = request != null
                ? request.getMethod() + ":" + request.getRequestURI()
                : joinPoint.getSignature().toShortString();
        String dimension;
        switch (rateLimiter.limitType()) {
            case GLOBAL -> dimension = "global";
            case IP -> dimension = WebRequestUtils.resolveClientIp(request);
            default -> {
                OperatorSupplier supplier = operatorSupplierProvider.getIfAvailable();
                dimension = WebRequestUtils.resolveOperator(supplier, request);
            }
        }
        // 窗口编号编入键，滚动换窗
        long windowMillis = Math.max(1, rateLimiter.time()) * 1000L;
        long bucket = System.currentTimeMillis() / windowMillis;
        return KEY_PREFIX + dimension + ":" + method + ":" + bucket;
    }
}

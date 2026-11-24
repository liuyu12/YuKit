package cn.lycoder.boot.framework.protection.config;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.protection.idempotent.IdempotentAspect;
import cn.lycoder.boot.framework.protection.ratelimiter.RateLimiterAspect;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 服务保障自动配置：装配幂等与限流切面
 * <p>
 * 需要 classpath 同时存在 AspectJ 与 Spring Data Redis（{@link StringRedisTemplate}）。切面对
 * {@code StringRedisTemplate}/{@code OperatorSupplier} 采用 {@code ObjectProvider} 延迟解析——
 * <b>刻意不用 {@code @ConditionalOnBean(StringRedisTemplate)}</b>：该条件在自动配置评估期可能因
 * Redis 自动配置尚未注册而误判，导致切面被跳过（与缓存 starter 同源的经验教训）。
 * <p>
 * 通过 {@code yukit.protection.enabled=false} 可整体关闭。
 */
@AutoConfiguration
@ConditionalOnClass({Aspect.class, StringRedisTemplate.class})
@ConditionalOnProperty(prefix = "yukit.protection", name = "enabled", matchIfMissing = true)
public class YukitProtectionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdempotentAspect.class)
    public IdempotentAspect idempotentAspect(ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                                             ObjectProvider<OperatorSupplier> operatorSupplierProvider) {
        return new IdempotentAspect(redisTemplateProvider, operatorSupplierProvider);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiterAspect.class)
    public RateLimiterAspect rateLimiterAspect(ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                                               ObjectProvider<OperatorSupplier> operatorSupplierProvider) {
        return new RateLimiterAspect(redisTemplateProvider, operatorSupplierProvider);
    }
}

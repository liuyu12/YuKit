package cn.lycoder.boot.framework.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 缓存自动配置：基于 Spring Cache 抽象，后端可切换（{@code yukit.cache.type}）
 * <p>
 * - {@code caffeine}（默认）：本地 JVM 堆内缓存，装配 {@link CaffeineCacheManager}，
 * 规格来自 {@code yukit.cache.spec}，未单独声明的缓存名套用默认，{@code yukit.cache.caches} 按名覆盖；
 * - {@code redis}：集中式缓存，装配 {@link RedisCacheManager}，复用容器已有的
 * {@link RedisConnectionFactory}（Sa-Token 已引入），规格见 {@code yukit.cache.redis}。
 * <p>
 * 引入 {@link EnableCaching} 使 starter 开箱即用，业务侧 {@code @Cacheable} 直接生效，
 * 切换后端不改动任何业务代码。通过 {@code yukit.cache.enabled=false} 可整体关闭。
 * <p>
 * before {@code CacheAutoConfiguration}：项目已因 Sa-Token 引入 Redis，Boot 默认缓存探测
 * 会把 {@code @Cacheable} 挂到 RedisCacheManager；此处抢先按 {@code type} 装配对应管理器，
 * {@code @ConditionalOnMissingBean(CacheManager)} 令 Boot 默认让位（Sa-Token 会话仍独立用 Redis，互不影响）。
 */
@AutoConfiguration(before = CacheAutoConfiguration.class)
@EnableCaching
@ConditionalOnProperty(prefix = "yukit.cache", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(YukitCacheProperties.class)
public class YukitCacheAutoConfiguration {

    /**
     * Caffeine 本地缓存后端（默认）
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(CaffeineCacheManager.class)
    @ConditionalOnProperty(prefix = "yukit.cache", name = "type", havingValue = "caffeine", matchIfMissing = true)
    static class CaffeineCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean(CacheManager.class)
        public CacheManager cacheManager(YukitCacheProperties properties) {
            CaffeineCacheManager cacheManager = new CaffeineCacheManager();

            // 默认规格：未单独声明的缓存名动态创建时套用
            Caffeine<Object, Object> defaultCaffeine = Caffeine.from(properties.getSpec());
            if (properties.isRecordStats()) {
                defaultCaffeine.recordStats();
            }
            cacheManager.setCaffeine(defaultCaffeine);

            // 差异化规格：按缓存名预注册专属缓存，覆盖默认 spec
            for (Map.Entry<String, String> entry : properties.getCaches().entrySet()) {
                Caffeine<Object, Object> caffeine = Caffeine.from(entry.getValue());
                if (properties.isRecordStats()) {
                    caffeine.recordStats();
                }
                cacheManager.registerCustomCache(entry.getKey(), caffeine.build());
            }
            return cacheManager;
        }
    }

    /**
     * Redis 集中式缓存后端：仅当 {@code yukit.cache.type=redis} 时装配。
     * <p>
     * 不加 {@code @ConditionalOnBean(RedisConnectionFactory)}：该条件在评估期可
     * 能因 Redis 自动配置尚未注册而误判为假；改为方法参数注入，Spring 会在创建本
     * Bean 时先行解析依赖，不受自动配置顺序影响。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnProperty(prefix = "yukit.cache", name = "type", havingValue = "redis")
    static class RedisCacheConfigurationSpec {

        @Bean
        @ConditionalOnMissingBean(CacheManager.class)
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory, YukitCacheProperties properties) {
            YukitCacheProperties.Redis redis = properties.getRedis();

            // 默认规格：key 前缀 + 默认 TTL（0/负值表示不过期，沿用 RedisCacheConfiguration 默认）
            RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                    .prefixCacheNameWith(redis.getKeyPrefix());
            base = applyTtl(base, redis.getTimeToLive());
            final RedisCacheConfiguration defaults = base;

            // 按缓存名覆盖 TTL
            Map<String, RedisCacheConfiguration> perCache = new LinkedHashMap<>();
            redis.getTtlByCache().forEach((name, ttl) -> perCache.put(name, applyTtl(defaults, ttl)));

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaults)
                    .withInitialCacheConfigurations(perCache)
                    .build();
        }

        private RedisCacheConfiguration applyTtl(RedisCacheConfiguration config, Duration ttl) {
            return (ttl == null || ttl.isZero() || ttl.isNegative()) ? config : config.entryTtl(ttl);
        }
    }

}

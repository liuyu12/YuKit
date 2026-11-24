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

import java.util.Map;

/**
 * Caffeine 缓存自动配置
 * <p>
 * 装配一个 {@link CaffeineCacheManager}：默认规格来自 {@code yukit.cache.spec}，
 * 未在默认规格下的缓存名可在 {@code yukit.cache.caches} 里按名覆盖 TTL/容量。
 * <p>
 * 引入 {@link EnableCaching} 使 starter 开箱即用，业务侧直接 {@code @Cacheable} 即可，
 * 无需再手动开启。通过 {@code yukit.cache.enabled=false} 可整体关闭。
 * <p>
 * before {@code CacheAutoConfiguration}：项目已因 Sa-Token 引入 Redis，Boot 默认的缓存探测
 * 会把 {@code @Cacheable} 挂到 RedisCacheManager；此处抢先装配 Caffeine，令 Spring Cache 走本地缓存
 * （Sa-Token 会话仍独立使用 Redis，两者互不影响）。
 */
@AutoConfiguration(before = CacheAutoConfiguration.class)
@EnableCaching
@ConditionalOnClass(CaffeineCacheManager.class)
@ConditionalOnProperty(prefix = "yukit.cache", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(YukitCacheProperties.class)
public class YukitCacheAutoConfiguration {

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

package cn.lycoder.boot.framework.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 缓存配置项
 * <p>
 * 基于 Spring Cache 抽象 + Caffeine 本地缓存。默认所有缓存名走 {@link #spec}，
 * 需要差异化（不同 TTL / 容量）时在 {@link #caches} 中按缓存名覆盖。
 */
@Data
@ConfigurationProperties(prefix = "yukit.cache")
public class YukitCacheProperties {

    /**
     * 是否开启缓存模块，默认开启
     */
    private boolean enabled = true;

    /**
     * 默认 Caffeine 规格，用于未在 {@link #caches} 中单独声明的缓存名。
     * <p>
     * 语法见 CaffeineSpec：maximumSize / expireAfterWrite / expireAfterAccess 等。
     * 默认最多 500 条、写入后 10 分钟过期。
     */
    private String spec = "maximumSize=500,expireAfterWrite=10m";

    /**
     * 按缓存名差异化配置：key 为 @Cacheable 里的 cacheNames，value 为该缓存专属的 Caffeine 规格。
     * <p>
     * 例：{@code dict: maximumSize=200,expireAfterWrite=1h}
     */
    private Map<String, String> caches = new LinkedHashMap<>();

    /**
     * 是否开启命中率统计（调试/监控用），开启后可从 CacheManager 取 stats
     */
    private boolean recordStats = false;

}

package cn.lycoder.boot.server.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.alone.SaAloneRedisInject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Sa-Token Alone 独立 Redis 装配（官方 sa-token-alone-redis-by-spring-boot4）
 * <p>
 * 该插件无自动配置，需手动注册 {@link SaAloneRedisInject}：它在 {@code @PostConstruct}
 * 阶段读取 {@code sa-token.alone-redis.*}（结构同 {@code spring.data.redis}），把 Sa-Token
 * 会话所用的 {@link SaTokenDao} 连接重定向到独立的 Redis 库，实现“权限会话与业务缓存分库”。
 * <p>
 * 仅在配置了 {@code sa-token.alone-redis.database} 时启用（opt-in）；去掉该配置即回退为
 * 会话与业务共用主 Redis，行为与未引入本插件一致。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SaAloneRedisInject.class)
@ConditionalOnProperty(prefix = "sa-token.alone-redis", name = "database")
public class SaTokenAloneRedisConfig {

    @Bean
    public SaAloneRedisInject saAloneRedisInject(SaTokenDao saTokenDao, Environment environment) {
        return new SaAloneRedisInject(saTokenDao, environment);
    }

}

package cn.lycoder.boot.server.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Sa-Token 持久层装配
 * <p>
 * 用 {@link KeyPrefixSaTokenDao} 顶替 sa-token-redis-template 自动装配的
 * {@link SaTokenDaoForRedisTemplate}（后者无 key 前缀能力），使会话数据
 * 统一挂在 {@code sa-token.key-prefix} 之下。
 * <p>
 * 自动装配的原始 Bean 仍在容器中但不被使用：{@code @Primary} 保证
 * sa-token 的 SaBeanInject 与本类的 {@code SaAloneRedisInject} 均选中带前缀的实现。
 */
@Configuration(proxyBeanMethods = false)
public class SaTokenDaoConfig {

    @Bean
    @Primary
    public SaTokenDao saTokenDao(@Value("${sa-token.key-prefix:yukit:}") String keyPrefix) {
        return new KeyPrefixSaTokenDao(keyPrefix);
    }
}

package cn.lycoder.boot.server.config;

import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate;

/**
 * 支持自定义 Redis key 前缀的 Sa-Token 持久层
 * <p>
 * sa-token 1.46 的 {@code SaTokenConfig} 没有 keyPrefix 配置项，官方插件把
 * {@link SaTokenDaoForRedisTemplate#wrapKey(String)} 留作覆写扩展点（默认原样返回）。
 * 这里覆写该方法，让所有读写（get/set/delete/getTimeout/searchData 等内部都走 wrapKey）
 * 统一挂上前缀，多项目共用 Redis 时隔离命名空间。
 * <p>
 * 注意：{@code SaAloneRedisInject} 会把 Dao 强转回本类型做分库重定向，子类实例天然兼容。
 */
public class KeyPrefixSaTokenDao extends SaTokenDaoForRedisTemplate {

    /**
     * Redis key 统一前缀，最终 key 形如 {@code yukit:satoken:login:token:xxx}
     */
    private final String keyPrefix;

    public KeyPrefixSaTokenDao(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    @Override
    public String wrapKey(String key) {
        return keyPrefix + key;
    }
}

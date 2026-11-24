package cn.lycoder.boot.framework.protection.ratelimiter;

/**
 * 限流维度
 */
public enum LimitType {

    /**
     * 按操作人限流（未登录时回退按 IP）
     */
    DEFAULT,

    /**
     * 按客户端 IP 限流
     */
    IP,

    /**
     * 全局限流（整个接口共享一个配额）
     */
    GLOBAL
}

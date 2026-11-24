package cn.lycoder.boot.framework.protection.ratelimiter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * <p>
 * 标注在方法上，由 {@link RateLimiterAspect} 基于 Redis 以固定窗口计数实现限流：
 * 在 {@link #time()} 秒的窗口内，同一维度（{@link #limitType()}）最多允许 {@link #count()} 次调用，
 * 超出即拒绝。计数通过 Lua 脚本原子完成（INCR + 首次 EXPIRE），无并发竞态。
 * <p>
 * Redis 缺席时自动放行，不阻断业务。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 窗口内允许的最大请求数
     */
    int count() default 100;

    /**
     * 统计窗口（秒）
     */
    int time() default 1;

    /**
     * 限流维度，默认按操作人
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 被限流时的提示语
     */
    String message() default "请求过于频繁，请稍后重试";
}

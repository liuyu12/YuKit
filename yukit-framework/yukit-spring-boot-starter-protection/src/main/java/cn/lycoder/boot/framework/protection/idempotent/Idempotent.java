package cn.lycoder.boot.framework.protection.idempotent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等（重复提交拦截）注解
 * <p>
 * 标注在方法上，由 {@link IdempotentAspect} 基于 Redis 在「操作人 + 请求地址 + 入参指纹」维度设置一个
 * 带 TTL 的占位键；窗口内的重复请求因占位失败被拒绝，实现防抖 / 防重复提交。
 * <p>
 * 语义：请求成功则在时间窗口内屏蔽同参重复；请求抛出业务异常时立即释放键，允许用户马上重试。
 * Redis 缺席时（未装配 {@code StringRedisTemplate}）自动放行，不阻断业务。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等时间窗口（秒），窗口内相同指纹的请求视为重复
     */
    int timeout() default 1;

    /**
     * 命中重复时的提示语
     */
    String message() default "重复请求，请稍后重试";
}

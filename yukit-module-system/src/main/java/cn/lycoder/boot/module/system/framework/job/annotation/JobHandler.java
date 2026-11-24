package cn.lycoder.boot.module.system.framework.job.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 Spring Bean 的方法可被定时任务调度中心反射调用
 * <p>
 * 必须显式标注，否则 {@code invoke_target} 指向该方法时拒绝执行，避免任意方法调用带来的安全风险。
 * 方法签名建议：{@code public void xxx()} 或 {@code public void xxx(Object... args)}，
 * 返回值会被忽略。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobHandler {

    /**
     * 任务用途描述，便于审计
     */
    String value() default "";
}

package cn.lycoder.boot.framework.common.operatelog;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，由 web starter 的 AOP 切面拦截，自动采集请求上下文
 * （操作人、URL、IP、耗时、入参/结果、成功失败）并经 {@link OperationLogRecorder} SPI 落库。
 * <p>
 * 采用「显式标注 + opt-in」设计：只有加了本注解的方法才记录，避免查询类接口产生海量噪音；
 * 业务模块只依赖本注解（在 common 中），不感知落库实现，符合项目的 SPI 解耦约定。
 *
 * @see OperationLogRecorder
 * @see OperationType
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 所属模块/功能，如「用户管理」「字典类型」
     */
    String module() default "";

    /**
     * 具体操作名，如「创建用户」「删除字典类型」
     */
    String name() default "";

    /**
     * 操作类型分类，默认 {@link OperationType#OTHER}
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 是否记录请求入参（敏感字段由切面统一脱敏、超长截断）
     */
    boolean recordParams() default true;

    /**
     * 是否记录响应结果（大结果/下载类接口可置 false 省空间）
     */
    boolean recordResult() default true;
}

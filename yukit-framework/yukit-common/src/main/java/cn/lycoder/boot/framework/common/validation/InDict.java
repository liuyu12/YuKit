package cn.lycoder.boot.framework.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字典值白名单校验：字段值必须属于指定数据字典类型（{@link #value()}）下的合法键值
 * <p>
 * 与 {@link InEnum} 的区别：InEnum 的白名单来自代码里的固定枚举，随发版变更；
 * InDict 的白名单来自运营可在「字典管理」维护的 sys_dict_data，动态生效，适合
 * 下拉选项、状态码等业务可配置字段。
 * <p>
 * 用法：{@code @InDict("sys_user_sex") private String sex;}
 * <p>
 * 约定：null/空串视为通过（必填请叠加 @NotBlank）；字典 Bean 未装配或该类型无启用项时
 * 按 fail-open 通过，不拦截。
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InDictValidator.class)
public @interface InDict {

    /**
     * @return 字典类型标识，对应 sys_dict_type.dict_type
     */
    String value();

    String message() default "取值不在字典允许范围内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

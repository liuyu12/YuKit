package cn.lycoder.boot.framework.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字符串集合白名单校验：字段值必须在 {@link #value()} 列举的范围内
 * <p>
 * 适合无枚举定义的前端下拉/字典类字段，如 {@code @InSet({"M", "F"})}；
 * 有枚举时优先用 {@link InEnum}
 * <p>
 * null/空串视为通过（可选字段），必填请叠加 @NotBlank
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InSetValidator.class)
public @interface InSet {

    /**
     * @return 合法值列表
     */
    String[] value();

    String message() default "必须在取值范围 {value} 内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

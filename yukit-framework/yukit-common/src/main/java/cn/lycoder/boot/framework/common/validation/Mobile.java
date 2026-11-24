package cn.lycoder.boot.framework.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 手机号校验：11 位、1 开头、第二位 3-9（覆盖三大运营商及虚拟运营商号段）
 * <p>
 * 用法：{@code @Mobile(message = "手机号格式不正确") private String mobile;}
 * <p>
 * null/空串视为通过（可选字段），必填请叠加 @NotBlank
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MobileValidator.class)
public @interface Mobile {

    String message() default "手机号格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

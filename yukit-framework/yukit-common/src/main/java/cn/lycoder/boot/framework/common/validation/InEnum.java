package cn.lycoder.boot.framework.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 枚举值白名单校验：字段值必须在指定枚举（实现 {@link ArrayValuable}）的取值范围内
 * <p>
 * 用法：{@code @InEnum(CommonStatusEnum.class) private Integer status;}
 * <p>
 * null 视为通过（必填请叠加 @NotNull），List 场景则要求逐元素命中白名单
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InEnumValidator.class)
public @interface InEnum {

    /**
     * @return 实现 ArrayValuable 接口的枚举类
     */
    Class<? extends ArrayValuable<?>> value();

    String message() default "必须在指定范围 {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package cn.lycoder.boot.framework.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

/**
 * {@link InEnum} 校验器
 * <p>
 * 消息里的 {value} 占位符无法被 Jakarta EL 解析 Class 类型，
 * 因此在 initialize 阶段手工替换为枚举实际取值列表
 */
public class InEnumValidator implements ConstraintValidator<InEnum, Object> {

    private List<?> values;
    private String message;

    @Override
    public void initialize(InEnum annotation) {
        ArrayValuable<?> arrayValuable = null;
        Class<?> type = annotation.value();
        try {
            if (type.isEnum()) {
                // 枚举禁止反射 newInstance（IllegalAccessException），取第一个常量调 array()
                Object[] constants = type.getEnumConstants();
                if (constants != null && constants.length > 0) {
                    arrayValuable = (ArrayValuable<?>) constants[0];
                }
            } else {
                // 普通实现类（如字典常量类）走无参构造
                var constructor = type.getDeclaredConstructor();
                constructor.setAccessible(true);
                arrayValuable = (ArrayValuable<?>) constructor.newInstance();
            }
        } catch (Exception ignored) {
            // 初始化失败时降级为空白名单（非 null 值一律校验不通过）
        }
        this.values = arrayValuable == null ? List.of() : Arrays.asList(arrayValuable.array());
        this.message = annotation.message().replace("{value}", this.values.toString());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // null 视为通过，必填由 @NotNull/@NotBlank 负责
        if (value == null) {
            return true;
        }
        boolean valid = value instanceof List<?> list
                ? list.stream().allMatch(values::contains)
                : values.contains(value);
        if (!valid) {
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }
        return valid;
    }
}

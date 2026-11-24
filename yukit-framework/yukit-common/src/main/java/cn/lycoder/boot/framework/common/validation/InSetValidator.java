package cn.lycoder.boot.framework.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

/**
 * {@link InSet} 校验器
 * <p>
 * 消息里的 {value} 占位符无法被 Jakarta EL 渲染数组，initialize 阶段手工替换
 */
public class InSetValidator implements ConstraintValidator<InSet, CharSequence> {

    private List<String> values;
    private String message;

    @Override
    public void initialize(InSet annotation) {
        this.values = Arrays.asList(annotation.value());
        this.message = annotation.message().replace("{value}", values.toString());
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        // null/空串视为通过：可选字段场景，必填由 @NotBlank 负责
        if (value == null || value.isEmpty()) {
            return true;
        }
        boolean valid = values.contains(value.toString());
        if (!valid) {
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }
        return valid;
    }
}

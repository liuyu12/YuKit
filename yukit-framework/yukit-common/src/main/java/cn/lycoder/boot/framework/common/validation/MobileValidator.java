package cn.lycoder.boot.framework.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * {@link Mobile} 校验器
 */
public class MobileValidator implements ConstraintValidator<Mobile, CharSequence> {

    /**
     * 中国大陆手机号：11 位、1 开头、第二位 3-9
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        // null/空串视为通过：可选字段场景，必填由 @NotBlank 负责
        if (value == null || value.isEmpty()) {
            return true;
        }
        return MOBILE_PATTERN.matcher(value).matches();
    }
}

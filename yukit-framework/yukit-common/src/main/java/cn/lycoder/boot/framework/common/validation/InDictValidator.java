package cn.lycoder.boot.framework.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Set;

/**
 * {@link InDict} 校验器
 * <p>
 * 通过 {@link DictValueProvider} SPI 动态获取字典合法取值。该 SPI 由字典宿主模块
 * 实现并注册为 Bean；此处用 {@link ObjectProvider} 构造器注入，保证在未装配字典能力的
 * 模块中使用本注解也不会启动失败。
 * <p>
 * 依赖 Spring Boot 的 SpringConstraintValidatorFactory（LocalValidatorFactoryBean 默认装配），
 * 其 getInstance 以 autowireConstructor 方式创建校验器，故本类采用构造器注入。
 */
@RequiredArgsConstructor
public class InDictValidator implements ConstraintValidator<InDict, CharSequence> {

    private final ObjectProvider<DictValueProvider> dictValueProvider;

    private String dictType;

    @Override
    public void initialize(InDict annotation) {
        this.dictType = annotation.value();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        // null/空串视为通过：可选字段场景，必填由 @NotBlank 负责
        if (value == null || value.isEmpty()) {
            return true;
        }
        DictValueProvider provider = dictValueProvider.getIfAvailable();
        if (provider == null) {
            // 字典 SPI 未装配（如当前应用未引入字典模块）：fail-open，不拦截
            return true;
        }
        Set<String> allowed = provider.getDictValues(dictType);
        if (allowed == null || allowed.isEmpty()) {
            // 字典类型未配置或无启用项：视为尚未定义约束，fail-open，避免误杀全部请求
            return true;
        }
        boolean valid = allowed.contains(value.toString());
        if (!valid) {
            context.buildConstraintViolationWithTemplate(
                            "取值必须属于字典[" + dictType + "]的范围 " + allowed)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }
        return valid;
    }
}

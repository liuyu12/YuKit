package cn.lycoder.boot.module.system.framework.dict;

import cn.lycoder.boot.framework.common.validation.DictValueProvider;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataRespVO;
import cn.lycoder.boot.module.system.service.dict.DictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link DictValueProvider} 的系统字典实现
 * <p>
 * 桥接带缓存的 {@link DictDataService#getDictDataListByType(String)}（仅返回启用项），
 * 使 {@code @InDict} 校验的合法取值随「字典管理」的维护动态生效，无需改代码发版。
 */
@Component
@RequiredArgsConstructor
public class DictValueProviderImpl implements DictValueProvider {

    private final DictDataService dictDataService;

    @Override
    public Set<String> getDictValues(String dictType) {
        return dictDataService.getDictDataListByType(dictType).stream()
                .map(DictDataRespVO::getDictValue)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}

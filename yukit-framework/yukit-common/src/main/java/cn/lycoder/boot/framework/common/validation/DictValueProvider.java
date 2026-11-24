package cn.lycoder.boot.framework.common.validation;

import java.util.Set;

/**
 * 字典取值提供者 SPI
 * <p>
 * 由字典能力的宿主模块（如 yukit-module-system）实现并注册为 Bean，
 * 内部通常委托带缓存的字典只读接口；{@link InDict} 校验器通过该接口
 * 动态获取某个字典类型下的合法键值集合，从而把"取值白名单"从硬编码枚举
 * 解耦为运营可维护的数据字典。
 * <p>
 * 约定：实现方只返回「启用状态」的字典项；无匹配字典或未装配实现时，
 * 校验器按 fail-open 处理（不拦截），避免因字典未初始化而误杀正常请求。
 */
public interface DictValueProvider {

    /**
     * 获取指定字典类型下全部合法的键值集合
     *
     * @param dictType 字典类型标识，如 {@code sys_user_sex}
     * @return 合法键值集合；字典未配置或无启用项时返回空集合
     */
    Set<String> getDictValues(String dictType);
}

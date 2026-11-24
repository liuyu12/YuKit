package cn.lycoder.boot.module.system.enums.common;

import cn.lycoder.boot.framework.common.validation.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 通用状态枚举（账号状态、菜单状态等 0/1 场景复用）
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum implements ArrayValuable<String> {

    ENABLE("0", "正常"),
    DISABLE("1", "停用");

    /**
     * 状态值
     */
    private final String status;
    /**
     * 状态名
     */
    private final String name;

    /**
     * 懒计算取值：不能用静态常量数组缓存，静态初始化早于枚举常量，届时 values() 为空
     */
    @Override
    public String[] array() {
        return Arrays.stream(values()).map(CommonStatusEnum::getStatus).toArray(String[]::new);
    }
}

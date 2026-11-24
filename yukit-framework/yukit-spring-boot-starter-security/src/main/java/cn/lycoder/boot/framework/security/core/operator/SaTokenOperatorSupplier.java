package cn.lycoder.boot.framework.security.core.operator;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.security.core.util.SecurityFrameworkUtils;

/**
 * 基于 Sa-Token 的操作人提供者实现
 * <p>
 * 注册为 Bean 后，被 yukit-spring-boot-starter-mybatis 的审计字段自动填充消费；
 * 未登录（如定时任务、启动初始化）时返回 null，由消费方决定回退值。
 */
public class SaTokenOperatorSupplier implements OperatorSupplier {

    @Override
    public String get() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return userId == null ? null : String.valueOf(userId);
    }
}

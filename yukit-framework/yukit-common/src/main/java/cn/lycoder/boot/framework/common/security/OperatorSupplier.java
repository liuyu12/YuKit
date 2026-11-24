package cn.lycoder.boot.framework.common.security;

/**
 * 操作人提供者 SPI
 * <p>
 * 由安全模块（如 yukit-spring-boot-starter-security）实现并注册为 Bean，
 * 数据层审计字段自动填充等场景通过该接口获取当前操作人，无需感知安全框架实现。
 */
@FunctionalInterface
public interface OperatorSupplier {

    /**
     * 获取当前操作人标识（用户名 / 用户 ID）
     *
     * @return 当前操作用户，无上下文时返回 null
     */
    String get();

    /**
     * 默认实现：始终返回 "system"
     */
    static OperatorSupplier system() {
        return () -> "system";
    }
}

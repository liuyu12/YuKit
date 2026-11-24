package cn.lycoder.boot.framework.common.operatelog;

/**
 * 操作日志记录器 SPI
 * <p>
 * 由业务模块（如 yukit-module-system）实现并注册为 Bean，负责把 {@link OperationLogRecord} 落库；
 * web starter 的切面通过 {@code ObjectProvider} 延迟获取本接口，缺席时静默跳过（fail-open），
 * 保证「未接持久化」不影响业务，也避免 web starter 反向依赖具体存储实现。
 * <p>
 * 与 {@code DictValueProvider}、{@code OperatorSupplier} 同属项目的 SPI 解耦约定。
 */
@FunctionalInterface
public interface OperationLogRecorder {

    /**
     * 记录一条操作日志。实现方需自行保证不抛出异常中断业务（切面已做兜底，但实现也应健壮）。
     *
     * @param record 采集完成的日志记录
     */
    void record(OperationLogRecord record);
}

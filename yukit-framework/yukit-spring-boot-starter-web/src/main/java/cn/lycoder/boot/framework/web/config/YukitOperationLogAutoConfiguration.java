package cn.lycoder.boot.framework.web.config;

import cn.lycoder.boot.framework.common.operatelog.OperationLogRecorder;
import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.web.operatelog.OperationLogAspect;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;

/**
 * 操作日志自动配置
 * <p>
 * classpath 存在 AspectJ（随 aspectjweaver）且未显式关闭时装配 {@link OperationLogAspect}。
 * 切面依赖的 {@link OperationLogRecorder}、{@link OperatorSupplier}、{@link ObjectMapper} 均通过
 * {@code ObjectProvider} 延迟解析，任一缺席都能优雅降级，故本配置不强依赖这些 Bean 是否存在。
 * {@link Clock} 由本 starter 的 {@link YukitClockAutoConfiguration} 无条件装配（yukit.zone-id 配置），直接注入。
 * <p>
 * 通过 {@code yukit.operation-log.enabled=false} 可整体关闭。
 */
@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@ConditionalOnProperty(prefix = "yukit.operation-log", name = "enabled", matchIfMissing = true)
public class YukitOperationLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OperationLogAspect.class)
    public OperationLogAspect operationLogAspect(ObjectProvider<OperationLogRecorder> recorderProvider,
                                                 ObjectProvider<OperatorSupplier> operatorSupplierProvider,
                                                 ObjectProvider<ObjectMapper> objectMapperProvider,
                                                 Clock clock) {
        return new OperationLogAspect(recorderProvider, operatorSupplierProvider, objectMapperProvider, clock);
    }
}

package cn.lycoder.boot.framework.web.config;

import cn.lycoder.boot.framework.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Web 模块自动配置
 */
@AutoConfiguration
public class YukitWebAutoConfiguration implements EnvironmentAware {

    private Environment environment;

    /**
     * 全局异常处理器
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler(environment);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}

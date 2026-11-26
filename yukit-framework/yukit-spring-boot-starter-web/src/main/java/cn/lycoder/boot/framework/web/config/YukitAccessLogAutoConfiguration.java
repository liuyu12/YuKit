package cn.lycoder.boot.framework.web.config;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.web.filter.AccessLogFilter;
import cn.lycoder.boot.framework.web.filter.AccessLogInterceptor;
import jakarta.servlet.DispatcherType;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 接口访问日志自动配置
 * <p>
 * 装配 {@link AccessLogFilter}（包装响应、traceId、计时）与 {@link AccessLogInterceptor}（输出摘要、读取登录用户）。
 * 通过 {@code yukit.access-log.enabled=false} 可整体关闭。
 */
@AutoConfiguration
@ConditionalOnClass(OncePerRequestFilter.class)
@ConditionalOnProperty(prefix = "yukit.access-log", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(YukitAccessLogProperties.class)
public class YukitAccessLogAutoConfiguration {

    @Bean
    public FilterRegistrationBean<AccessLogFilter> accessLogFilterRegistration(YukitAccessLogProperties properties) {
        FilterRegistrationBean<AccessLogFilter> registration =
                new FilterRegistrationBean<>(new AccessLogFilter(properties));
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setName("accessLogFilter");
        // 最外层执行，保证计时覆盖完整链路且 MDC traceId 尽早写入
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public WebMvcConfigurer accessLogInterceptorConfigurer(
            YukitAccessLogProperties properties, ObjectProvider<OperatorSupplier> operatorSupplierProvider) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(new AccessLogInterceptor(properties, operatorSupplierProvider));
            }
        };
    }
}

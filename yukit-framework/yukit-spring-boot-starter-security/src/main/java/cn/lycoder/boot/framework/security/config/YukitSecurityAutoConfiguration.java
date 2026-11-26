package cn.lycoder.boot.framework.security.config;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.security.core.handler.SaTokenExceptionHandler;
import cn.lycoder.boot.framework.security.core.operator.SaTokenOperatorSupplier;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 安全框架自动配置
 * <p>
 * 默认行为：全路由登录校验（白名单除外）+ 注解鉴权（@SaCheckLogin、@SaCheckPermission 等）。
 * 通过 yukit.security.enabled=false 可整体关闭。
 */
@AutoConfiguration
@EnableConfigurationProperties(YukitSecurityProperties.class)
@ConditionalOnProperty(prefix = "yukit.security", name = "enabled", matchIfMissing = true)
public class YukitSecurityAutoConfiguration {

    /**
     * 注册 Sa-Token 拦截器：路由鉴权 + 注解鉴权
     */
    @Bean
    public WebMvcConfigurer saTokenWebMvcConfigurer(YukitSecurityProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                String[] excludePaths = properties.getExcludePaths().toArray(String[]::new);
                registry.addInterceptor(new SaInterceptor(handle -> SaRouter
                                .match("/**")
                                .notMatch(excludePaths)
                                .check(r -> StpUtil.checkLogin())))
                        .addPathPatterns("/**");
            }
        };
    }

    /**
     * 操作人提供者：桥接审计字段自动填充（见 mybatis starter 的 DefaultMetaObjectHandler）
     */
    @Bean
    @ConditionalOnMissingBean(OperatorSupplier.class)
    public OperatorSupplier operatorSupplier() {
        return new SaTokenOperatorSupplier();
    }

    /**
     * Sa-Token 异常统一处理
     */
    @Bean
    @ConditionalOnMissingBean(SaTokenExceptionHandler.class)
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }
}

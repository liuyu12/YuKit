package cn.lycoder.boot.framework.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * OpenAPI 接口文档自动配置（springdoc-openapi）
 * <p>
 * 仅在 classpath 存在 springdoc（{@link OpenAPI}）时装配，业务侧无需任何改动即可获得
 * {@code /v3/api-docs} 与 Swagger UI。此处统一注入基于请求头的 Sa-Token 鉴权方案，
 * 使导入 Apifox / Postman 后只需设置一次 token 即可调试全部接口。
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
public class YukitSwaggerAutoConfiguration {

    /**
     * 鉴权方案名，同时也是请求头名称，需与 sa-token.token-name 保持一致
     */
    private static final String SECURITY_SCHEME_NAME = "satoken";

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI yukitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("YuKit 管理后台 API")
                        .version("1.0.0")
                        .description("基于 Spring Boot 4 + Sa-Token 的后台脚手架接口文档"))
                // 全局鉴权：请求头 satoken = 登录后返回的 token 值
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(SECURITY_SCHEME_NAME)
                                .description("登录后将 token 填入此请求头（sa-token.token-name）")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}

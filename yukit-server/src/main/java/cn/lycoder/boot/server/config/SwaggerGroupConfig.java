package cn.lycoder.boot.server.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档分组
 * <p>
 * 将「系统管理」与「其他模块」拆成两份独立文档，Swagger UI / Apifox 可按分组分别查看与同步。
 * 约定：系统模块统一占用 {@code /system/**} 前缀；其余接口（含未来新增业务模块）通过
 * {@code pathsToMatch("/**") + pathsToExclude("/system/**")} 自动兜底归入「其他模块」，
 * 新增模块无需改动此处。若某模块需要自己的独立分组，在此另加一个 {@link GroupedOpenApi} Bean 即可。
 */
@Configuration
public class SwaggerGroupConfig {

    /**
     * 系统管理分组：仅系统模块接口
     */
    @Bean
    public GroupedOpenApi systemApiGroup() {
        return GroupedOpenApi.builder()
                .group("system")
                .displayName("系统管理")
                .pathsToMatch("/system/**")
                .build();
    }

    /**
     * 其他模块分组：除 /system/** 外的全部接口，未来新增模块自动纳入
     */
    @Bean
    public GroupedOpenApi otherApiGroup() {
        return GroupedOpenApi.builder()
                .group("other")
                .displayName("其他模块")
                .pathsToMatch("/**")
                .pathsToExclude("/system/**")
                .build();
    }
}

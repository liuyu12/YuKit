package cn.lycoder.boot.framework.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全框架配置项
 */
@Data
@ConfigurationProperties(prefix = "yukit.security")
public class YukitSecurityProperties {

    /**
     * 是否开启安全模块，默认开启
     */
    private boolean enabled = true;

    /**
     * 免登录白名单路径（Ant 风格），默认包含错误页与静态资源
     * <p>
     * 业务白名单（如 /admin-api/system/auth/login）在 application.yml 中追加
     */
    private List<String> excludePaths = new ArrayList<>(List.of(
            "/error",
            "/favicon.ico",
            "/**/*.js",
            "/**/*.css",
            "/**/*.html"
    ));
}

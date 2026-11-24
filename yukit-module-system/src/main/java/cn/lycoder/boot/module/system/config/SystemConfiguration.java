package cn.lycoder.boot.module.system.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * system 模块基础配置
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("cn.lycoder.boot.module.system.dal.mapper")
public class SystemConfiguration {

    /**
     * 密码加密器：BCrypt（自带随机盐，同一密码每次加密结果不同）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

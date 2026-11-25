package cn.lycoder.boot.framework.web.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;

/**
 * 全局时间源自动配置
 * <p>
 * 统一提供一个带业务时区的 {@link Clock} Bean，业务代码注入 Clock 后调用
 * {@code LocalDateTime.now(clock)}，避免各处手写 ZoneId 或使用无参 now()
 * 导致行为随 JVM 默认时区漂移。时区通过 {@code yukit.zone-id} 配置，
 * 默认 Asia/Shanghai（与数据源 serverTimezone、自动填充处理器约定一致）。
 */
@AutoConfiguration
public class YukitClockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock clock(@Value("${yukit.zone-id:Asia/Shanghai}") String zoneId) {
        return Clock.system(ZoneId.of(zoneId));
    }
}

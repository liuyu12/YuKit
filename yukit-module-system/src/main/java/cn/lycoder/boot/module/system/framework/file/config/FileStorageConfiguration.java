package cn.lycoder.boot.module.system.framework.file.config;

import cn.lycoder.boot.module.system.framework.file.core.FileStorage;
import cn.lycoder.boot.module.system.framework.file.core.LocalFileStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储装配：按 {@code yukit.file.storage-type} 选择具体实现
 * <p>
 * 缺省即本地磁盘；后续接对象存储时新增对应 FileStorage Bean 与条件即可，
 * 业务侧（FileService）只依赖 SPI 无需改动。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FileProperties.class)
public class FileStorageConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "yukit.file", name = "storage-type", havingValue = FileStorage.TYPE_LOCAL, matchIfMissing = true)
    public FileStorage localFileStorage(FileProperties properties) {
        return new LocalFileStorage(properties.getLocal().getBasePath());
    }
}

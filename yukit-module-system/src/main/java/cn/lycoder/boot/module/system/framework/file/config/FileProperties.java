package cn.lycoder.boot.module.system.framework.file.config;

import cn.lycoder.boot.module.system.framework.file.core.FileStorage;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * 文件管理配置项（前缀 {@code yukit.file}）
 */
@ConfigurationProperties(prefix = "yukit.file")
@Data
public class FileProperties {

    /**
     * 存储类型，当前支持 local，后续可扩展（如 minio）
     */
    private String storageType = FileStorage.TYPE_LOCAL;

    /**
     * 禁止上传的扩展名黑名单（小写比对），拦截可执行/脚本类高危文件
     */
    private Set<String> forbiddenExtensions = Set.of(
            "jsp", "jspx", "exe", "bat", "cmd", "sh", "dll", "so", "com", "scr", "vbs", "wsf", "jar", "html", "htm");

    /**
     * 本地磁盘存储配置
     */
    private final Local local = new Local();

    @Data
    public static class Local {

        /**
         * 存储根目录，相对路径以进程工作目录为基准
         */
        private String basePath = "./data/file-storage";
    }
}

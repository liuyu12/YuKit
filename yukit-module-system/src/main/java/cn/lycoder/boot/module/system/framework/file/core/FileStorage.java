package cn.lycoder.boot.module.system.framework.file.core;

import java.io.IOException;

/**
 * 文件存储 SPI：屏蔽底层介质（本地磁盘 / 对象存储）差异
 * <p>
 * 约定：所有路径均为相对存储根目录的相对路径（如 {@code 2026/11/25/uuid.ext}），
 * 由上层 Service 生成，实现类负责防止相对路径越出根目录。
 */
public interface FileStorage {

    /**
     * 本地磁盘存储类型标识
     */
    String TYPE_LOCAL = "local";

    /**
     * 存储类型，与 sys_file.storage_type 对应
     */
    String getType();

    /**
     * 写入文件（覆盖同名相对路径）
     */
    void store(String relativePath, byte[] content) throws IOException;

    /**
     * 读取文件全部字节
     */
    byte[] read(String relativePath) throws IOException;

    /**
     * 删除文件，目标不存在时静默返回
     */
    void delete(String relativePath) throws IOException;
}

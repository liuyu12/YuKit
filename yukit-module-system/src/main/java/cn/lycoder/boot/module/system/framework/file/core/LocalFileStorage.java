package cn.lycoder.boot.module.system.framework.file.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地磁盘文件存储实现
 * <p>
 * 存储根目录由 {@code yukit.file.local.base-path} 指定；读写前统一做
 * normalize + startsWith 校验，杜绝相对路径穿越（{@code ../}）越出根目录。
 */
public class LocalFileStorage implements FileStorage {

    /**
     * 存储根目录（绝对路径、已规范化）
     */
    private final Path root;

    public LocalFileStorage(String basePath) {
        this.root = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @Override
    public String getType() {
        return TYPE_LOCAL;
    }

    @Override
    public void store(String relativePath, byte[] content) throws IOException {
        Path target = resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.write(target, content);
    }

    @Override
    public byte[] read(String relativePath) throws IOException {
        return Files.readAllBytes(resolve(relativePath));
    }

    @Override
    public void delete(String relativePath) throws IOException {
        Files.deleteIfExists(resolve(relativePath));
    }

    /**
     * 相对路径解析到根目录内，越界直接拒绝
     */
    private Path resolve(String relativePath) {
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("非法文件路径，疑似目录穿越: " + relativePath);
        }
        return target;
    }
}

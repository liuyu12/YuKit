package cn.lycoder.boot.module.system.framework.file.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LocalFileStorage 单测：@TempDir 真实磁盘往返，不起 Spring 上下文
 */
class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    private LocalFileStorage storage;

    @BeforeEach
    void setUp() {
        storage = new LocalFileStorage(tempDir.toString());
    }

    @Test
    void storeReadDeleteRoundTrip() throws IOException {
        String relativePath = "2026/11/25/abc.png";
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

        storage.store(relativePath, content);
        // 多级目录应由 store 自动创建
        assertThat(tempDir.resolve(relativePath)).exists();
        assertThat(storage.read(relativePath)).isEqualTo(content);

        storage.delete(relativePath);
        assertThat(tempDir.resolve(relativePath)).doesNotExist();
    }

    @Test
    void deleteMissingFileShouldBeSilent() throws IOException {
        storage.delete("2026/11/25/not-exists.png");
        // 幂等：无异常即通过，仅断言目录未被误建
        assertThat(Files.exists(tempDir.resolve("2026/11/25/not-exists.png"))).isFalse();
    }

    @Test
    void storeShouldRejectPathTraversal() {
        Path outside = tempDir.getParent();
        // resolve 基于存储根目录，"../" 直接指向根外，应被拒绝
        String traversal = "../escape.txt";

        assertThatThrownBy(() -> storage.store(traversal, "x".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(outside.resolve("escape.txt")).doesNotExist();
    }

    @Test
    void getTypeShouldReturnLocal() {
        assertThat(storage.getType()).isEqualTo(FileStorage.TYPE_LOCAL);
    }
}

package cn.lycoder.boot.module.system.service.file;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.ErrorCode;
import cn.lycoder.boot.module.system.dal.dataobject.file.SystemFileDO;
import cn.lycoder.boot.module.system.dal.mapper.file.SystemFileMapper;
import cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants;
import cn.lycoder.boot.module.system.framework.file.config.FileProperties;
import cn.lycoder.boot.module.system.framework.file.core.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FileServiceImpl 单测：纯 Mockito，不起 Spring 上下文
 */
@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    /**
     * "hello" 的已知 SHA-256 摘要，作为入库哈希断言锚点
     */
    private static final String HELLO_SHA256 = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
    private static final Pattern STORED_NAME_PATTERN = Pattern.compile("[0-9a-f]{32}\\.png");

    @Mock
    private SystemFileMapper fileMapper;
    @Mock
    private FileStorage fileStorage;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(fileMapper, fileStorage, new FileProperties());
    }

    @Test
    void createFileShouldStoreAndInsertMeta() throws IOException {
        when(fileStorage.getType()).thenReturn(FileStorage.TYPE_LOCAL);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "截图.png", "image/png", "hello".getBytes(StandardCharsets.UTF_8));

        SystemFileDO result = fileService.createFile(multipartFile);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileStorage).store(pathCaptor.capture(), eq("hello".getBytes(StandardCharsets.UTF_8)));
        String expectedDir = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(LocalDate.now()) + "/";
        assertThat(pathCaptor.getValue()).startsWith(expectedDir);
        assertThat(pathCaptor.getValue().substring(expectedDir.length())).matches(STORED_NAME_PATTERN);

        assertThat(result.getOriginalName()).isEqualTo("截图.png");
        assertThat(result.getMimeType()).isEqualTo("image/png");
        assertThat(result.getFileSize()).isEqualTo(5L);
        assertThat(result.getFileHash()).isEqualTo(HELLO_SHA256);
        assertThat(result.getStorageType()).isEqualTo(FileStorage.TYPE_LOCAL);
        assertThat(result.getBizPath()).isEqualTo(pathCaptor.getValue());
        verify(fileMapper).insert(result);
    }

    @Test
    void createFileShouldRejectEmpty() {
        MockMultipartFile empty = new MockMultipartFile("file", "a.png", "image/png", new byte[0]);

        assertServiceException(() -> fileService.createFile(empty), SystemErrorCodeConstants.FILE_EMPTY);
    }

    @Test
    void createFileShouldRejectForbiddenExtension() {
        MockMultipartFile jsp = new MockMultipartFile("file", "evil.jsp", "text/plain", "x".getBytes());

        assertServiceException(() -> fileService.createFile(jsp), SystemErrorCodeConstants.FILE_TYPE_FORBIDDEN);
    }

    @Test
    void createFileShouldWrapStorageIoError() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "a.png", "image/png", "x".getBytes());
        doThrow(new IOException("disk full")).when(fileStorage).store(any(), any());

        assertServiceException(() -> fileService.createFile(multipartFile),
                SystemErrorCodeConstants.FILE_STORAGE_FAILED);
        verify(fileMapper, never()).insert(any(SystemFileDO.class));
    }

    @Test
    void getFileContentShouldReturnBytes() throws IOException {
        SystemFileDO fileDO = new SystemFileDO();
        fileDO.setFileId(1L);
        fileDO.setBizPath("2026/11/25/a.png");
        when(fileMapper.selectById(1L)).thenReturn(fileDO);
        when(fileStorage.read("2026/11/25/a.png")).thenReturn("hello".getBytes(StandardCharsets.UTF_8));

        FileService.FileContent content = fileService.getFileContent(1L);

        assertThat(content.file()).isSameAs(fileDO);
        assertThat(content.content()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void getFileContentShouldRejectMissingRecord() {
        when(fileMapper.selectById(9L)).thenReturn(null);

        assertServiceException(() -> fileService.getFileContent(9L), SystemErrorCodeConstants.FILE_NOT_EXISTS);
    }

    @Test
    void deleteFileShouldRemoveStorageAndRow() throws IOException {
        SystemFileDO fileDO = new SystemFileDO();
        fileDO.setFileId(2L);
        fileDO.setBizPath("2026/11/25/b.png");
        when(fileMapper.selectById(2L)).thenReturn(fileDO);

        fileService.deleteFile(2L);

        verify(fileStorage).delete("2026/11/25/b.png");
        verify(fileMapper).deleteById(2L);
    }

    @Test
    void deleteFileShouldRejectMissingRecord() throws IOException {
        when(fileMapper.selectById(3L)).thenReturn(null);

        assertServiceException(() -> fileService.deleteFile(3L), SystemErrorCodeConstants.FILE_NOT_EXISTS);
        verify(fileStorage, never()).delete(any());
    }

    private static void assertServiceException(ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(errorCode.getCode());
    }
}

package cn.lycoder.boot.module.system.service.file;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.file.vo.FilePageReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.file.SystemFileDO;
import cn.lycoder.boot.module.system.dal.mapper.file.SystemFileMapper;
import cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants;
import cn.lycoder.boot.module.system.framework.file.config.FileProperties;
import cn.lycoder.boot.module.system.framework.file.core.FileStorage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * 文件管理实现
 * <p>
 * 安全要点：扩展名黑名单拦截高危文件；存储文件名用 UUID 重命名（原始名仅展示、
 * 入库前截断），路径按日期分目录并由 {@link FileStorage} 防穿越；内容哈希用 SHA-256。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    /**
     * 日期分目录格式，如 2026/11/25
     */
    private static final DateTimeFormatter DATE_DIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    /**
     * 原始文件名入库最大长度（对齐列宽 varchar(255)）
     */
    private static final int MAX_ORIGINAL_NAME_LENGTH = 255;
    /**
     * MIME 类型入库最大长度（对齐列宽 varchar(128)）
     */
    private static final int MAX_MIME_TYPE_LENGTH = 128;
    /**
     * SHA-256 摘要算法名（JDK 标准别名）
     */
    private static final String HASH_ALGORITHM = "SHA-256";

    private final SystemFileMapper fileMapper;
    private final FileStorage fileStorage;
    private final FileProperties fileProperties;

    @Override
    public SystemFileDO createFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException(SystemErrorCodeConstants.FILE_EMPTY);
        }
        String originalName = StringUtils.hasText(file.getOriginalFilename())
                ? extractFileName(file.getOriginalFilename()) : "unnamed";
        String extension = StringUtils.getFilenameExtension(originalName);
        if (extension != null && fileProperties.getForbiddenExtensions().contains(extension.toLowerCase(Locale.ROOT))) {
            log.warn("[createFile] 拦截禁用扩展名, originalName={}, extension={}", originalName, extension);
            throw new ServiceException(SystemErrorCodeConstants.FILE_TYPE_FORBIDDEN);
        }
        byte[] content = readBytes(file);

        String storedName = UUID.randomUUID().toString().replace("-", "")
                + (extension == null ? "" : "." + extension.toLowerCase(Locale.ROOT));
        String relativePath = DATE_DIR_FORMATTER.format(LocalDate.now()) + "/" + storedName;
        storeWithWrapper(relativePath, content);

        SystemFileDO fileDO = new SystemFileDO();
        fileDO.setFileName(storedName);
        fileDO.setOriginalName(truncate(originalName, MAX_ORIGINAL_NAME_LENGTH));
        fileDO.setBizPath(relativePath);
        fileDO.setStorageType(fileStorage.getType());
        fileDO.setMimeType(truncate(file.getContentType(), MAX_MIME_TYPE_LENGTH));
        fileDO.setFileSize((long) content.length);
        fileDO.setFileHash(sha256Hex(content));
        fileDO.setCreateTime(LocalDateTime.now());
        fileMapper.insert(fileDO);
        return fileDO;
    }

    @Override
    public SystemFileDO getFile(Long fileId) {
        return fileMapper.selectById(fileId);
    }

    @Override
    public PageResult<SystemFileDO> getFilePage(FilePageReqVO reqVO) {
        LambdaQueryWrapper<SystemFileDO> wrapper = Wrappers.<SystemFileDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getOriginalName()), SystemFileDO::getOriginalName, reqVO.getOriginalName())
                .like(StringUtils.hasText(reqVO.getMimeType()), SystemFileDO::getMimeType, reqVO.getMimeType())
                .ge(reqVO.getBeginTime() != null, SystemFileDO::getCreateTime, reqVO.getBeginTime())
                .le(reqVO.getEndTime() != null, SystemFileDO::getCreateTime, reqVO.getEndTime())
                .orderByDesc(SystemFileDO::getFileId);
        Page<SystemFileDO> page = fileMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public FileContent getFileContent(Long fileId) {
        SystemFileDO fileDO = validateFileExists(fileId);
        byte[] content = readWithWrapper(fileDO.getBizPath());
        return new FileContent(fileDO, content);
    }

    @Override
    public void deleteFile(Long fileId) {
        SystemFileDO fileDO = validateFileExists(fileId);
        deleteWithWrapper(fileDO.getBizPath());
        fileMapper.deleteById(fileId);
    }

    private SystemFileDO validateFileExists(Long fileId) {
        SystemFileDO fileDO = fileMapper.selectById(fileId);
        if (fileDO == null) {
            throw new ServiceException(SystemErrorCodeConstants.FILE_NOT_EXISTS);
        }
        return fileDO;
    }

    /**
     * 上传内容读取；IOException 视为存储失败（内容都拿不到，无需区分文案）
     */
    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.warn("[readBytes] 读取上传内容失败, name={}", file.getOriginalFilename(), e);
            throw new ServiceException(SystemErrorCodeConstants.FILE_STORAGE_FAILED);
        }
    }

    /**
     * 存储层 IOException 统一包装为业务异常，避免 IOException 泄漏到接口签名
     */
    private void storeWithWrapper(String relativePath, byte[] content) {
        try {
            fileStorage.store(relativePath, content);
        } catch (IOException e) {
            log.warn("[storeWithWrapper] 写入存储失败, path={}", relativePath, e);
            throw new ServiceException(SystemErrorCodeConstants.FILE_STORAGE_FAILED);
        }
    }

    private byte[] readWithWrapper(String relativePath) {
        try {
            return fileStorage.read(relativePath);
        } catch (IOException e) {
            log.warn("[readWithWrapper] 读取存储失败, path={}", relativePath, e);
            throw new ServiceException(SystemErrorCodeConstants.FILE_STORAGE_FAILED);
        }
    }

    private void deleteWithWrapper(String relativePath) {
        try {
            fileStorage.delete(relativePath);
        } catch (IOException e) {
            log.warn("[deleteWithWrapper] 删除存储失败, path={}", relativePath, e);
            throw new ServiceException(SystemErrorCodeConstants.FILE_STORAGE_FAILED);
        }
    }

    /**
     * 浏览器可能带完整路径（IE 上传），只取最后一段文件名
     */
    private static String extractFileName(String path) {
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private static String sha256Hex(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 为 JDK 必备算法，理论不可达
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", e);
        }
    }
}

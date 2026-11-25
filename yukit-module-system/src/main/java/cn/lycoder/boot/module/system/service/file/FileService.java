package cn.lycoder.boot.module.system.service.file;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.file.vo.FilePageReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.file.SystemFileDO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理 Service：元数据入 sys_file，内容走 FileStorage SPI
 */
public interface FileService {

    /**
     * 下载数据：文件元数据 + 内容字节
     */
    record FileContent(SystemFileDO file, byte[] content) {
    }

    /**
     * 上传文件：校验类型 → 存储内容 → 落元数据，返回入库后的 DO
     */
    SystemFileDO createFile(MultipartFile file);

    /**
     * 获取文件元数据（不存在返回 null）
     */
    SystemFileDO getFile(Long fileId);

    /**
     * 分页查询文件元数据
     */
    PageResult<SystemFileDO> getFilePage(FilePageReqVO reqVO);

    /**
     * 读取文件内容用于下载，文件记录或存储实体缺失时抛业务异常
     */
    FileContent getFileContent(Long fileId);

    /**
     * 预览用文件内容：仅限图片 MIME（inline 展示端点只靠登录态不鉴权，
     * 不收紧类型等于凭 ID 绕过下载权限读任意文件）
     */
    FileContent getPreviewableContent(Long fileId);

    /**
     * 删除文件（先删存储实体，再删元数据行）
     */
    void deleteFile(Long fileId);
}

package cn.lycoder.boot.module.system.controller.admin.file;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.file.vo.FilePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.file.vo.FileRespVO;
import cn.lycoder.boot.module.system.convert.file.FileConvert;
import cn.lycoder.boot.module.system.dal.dataobject.file.SystemFileDO;
import cn.lycoder.boot.module.system.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * 文件管理：上传 / 分页 / 详情 / 下载 / 删除
 * <p>
 * 内容通过后端流式下载（不暴露存储目录静态映射），权限码对齐 V5 菜单种子。
 */
@Tag(name = "文件管理")
@Validated
@RestController
@RequestMapping("/system/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileConvert fileConvert;

    @Operation(summary = "上传文件")
    @OperationLog(module = "文件管理", name = "上传文件", type = OperationType.INSERT)
    @PostMapping
    @SaCheckPermission("system:file:upload")
    public Result<FileRespVO> uploadFile(@Parameter(description = "上传文件") @RequestPart("file") MultipartFile file) {
        SystemFileDO fileDO = fileService.createFile(file);
        return Result.success(fileConvert.convert(fileDO));
    }

    @Operation(summary = "分页查询文件")
    @GetMapping
    @SaCheckPermission("system:file:list")
    public Result<PageResult<FileRespVO>> getFilePage(@Valid FilePageReqVO reqVO) {
        PageResult<SystemFileDO> page = fileService.getFilePage(reqVO);
        return Result.success(fileConvert.convertPage(page));
    }

    @Operation(summary = "获取文件详情")
    @GetMapping("/{fileId}")
    @SaCheckPermission("system:file:query")
    public Result<FileRespVO> getFile(@Parameter(description = "文件ID") @PathVariable("fileId") Long fileId) {
        SystemFileDO fileDO = fileService.getFile(fileId);
        return Result.success(fileDO == null ? null : fileConvert.convert(fileDO));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{fileId}/download")
    @SaCheckPermission("system:file:download")
    public ResponseEntity<Resource> downloadFile(@Parameter(description = "文件ID") @PathVariable("fileId") Long fileId) {
        FileService.FileContent fileContent = fileService.getFileContent(fileId);
        SystemFileDO fileDO = fileContent.file();
        String fileName = StringUtils.hasText(fileDO.getOriginalName()) ? fileDO.getOriginalName() : fileDO.getFileName();
        // filename(..., UTF_8) 由 Spring 生成 RFC 5987 编码，中文名下载不乱码
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        MediaType mediaType = StringUtils.hasText(fileDO.getMimeType())
                ? MediaType.parseMediaType(fileDO.getMimeType()) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(mediaType)
                .contentLength(fileContent.content().length)
                .body(new ByteArrayResource(fileContent.content()));
    }

    /**
     * 预览文件（仅图片，inline 展示）
     * <p>
     * 不挂按钮权限码：头像/列表缩略图等 <img> 标签无法携带令牌头之外的凭证，
     * 类型收紧由 FileService.getPreviewableContent 兜底（登录态仍必需）
     */
    @Operation(summary = "预览文件（仅图片）")
    @GetMapping("/{fileId}/preview")
    public ResponseEntity<Resource> previewFile(@Parameter(description = "文件ID") @PathVariable("fileId") Long fileId) {
        FileService.FileContent fileContent = fileService.getPreviewableContent(fileId);
        SystemFileDO fileDO = fileContent.file();
        MediaType mediaType = MediaType.parseMediaType(fileDO.getMimeType());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().build().toString())
                .contentType(mediaType)
                .contentLength(fileContent.content().length)
                .body(new ByteArrayResource(fileContent.content()));
    }

    @Operation(summary = "删除文件")
    @OperationLog(module = "文件管理", name = "删除文件", type = OperationType.DELETE)
    @DeleteMapping("/{fileId}")
    @SaCheckPermission("system:file:delete")
    public Result<Boolean> deleteFile(@Parameter(description = "文件ID") @PathVariable("fileId") Long fileId) {
        fileService.deleteFile(fileId);
        return Result.success(true);
    }
}

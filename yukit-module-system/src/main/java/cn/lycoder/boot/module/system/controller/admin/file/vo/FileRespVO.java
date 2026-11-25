package cn.lycoder.boot.module.system.controller.admin.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件 Response VO
 */
@Data
@Schema(description = "管理后台 - 文件 Response VO")
public class FileRespVO implements Serializable {

    @Schema(description = "文件ID", example = "1")
    private Long fileId;

    @Schema(description = "存储文件名", example = "f3b1c9a2.png")
    private String fileName;

    @Schema(description = "原始文件名", example = "截图.png")
    private String originalName;

    @Schema(description = "存储相对路径", example = "2026/11/25/f3b1c9a2.png")
    private String bizPath;

    @Schema(description = "存储类型", example = "local")
    private String storageType;

    @Schema(description = "MIME 类型", example = "image/png")
    private String mimeType;

    @Schema(description = "文件大小（字节）", example = "102400")
    private Long fileSize;

    @Schema(description = "文件内容 SHA-256", example = "e3b0c44298fc1c14...")
    private String fileHash;

    @Schema(description = "备注", example = "运营素材")
    private String remark;

    @Schema(description = "创建者", example = "admin")
    private String createBy;

    @Schema(description = "创建时间", example = "2026-11-25 13:45:00")
    private LocalDateTime createTime;
}

package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 磁盘分区监控信息
 */
@Data
@Schema(description = "管理后台 - 磁盘分区监控信息")
public class DiskRespVO {

    @Schema(description = "盘符/挂载点", example = "/")
    private String dirName;

    @Schema(description = "文件系统类型（hfs+/apfs/ntfs/ext4...）", example = "apfs")
    private String sysTypeName;

    @Schema(description = "卷类型（LOCAL/NETWORK...）", example = "LOCAL")
    private String typeName;

    @Schema(description = "总容量（GB）", example = "460.44")
    private Double total;

    @Schema(description = "已用容量（GB）", example = "213.21")
    private Double used;

    @Schema(description = "可用容量（GB）", example = "247.23")
    private Double free;

    @Schema(description = "使用率（%）", example = "46.31")
    private Double usage;
}

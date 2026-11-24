package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物理内存监控信息
 */
@Data
@Schema(description = "管理后台 - 物理内存监控信息")
public class MemRespVO {

    @Schema(description = "总内存（GB）", example = "16.00")
    private Double total;

    @Schema(description = "已用内存（GB）", example = "10.21")
    private Double used;

    @Schema(description = "可用内存（GB）", example = "5.79")
    private Double free;

    @Schema(description = "内存使用率（%）", example = "63.81")
    private Double usage;
}

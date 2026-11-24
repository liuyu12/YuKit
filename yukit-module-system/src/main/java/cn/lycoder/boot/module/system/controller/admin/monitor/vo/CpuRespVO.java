package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * CPU 监控信息
 * <p>
 * 数据来源 OSHI：{@code CentralProcessor.getSystemCpuLoad/getProcessorCounts}。
 */
@Data
@Schema(description = "管理后台 - CPU 监控信息")
public class CpuRespVO {

    @Schema(description = "CPU 名称（含主频）", example = "Apple M2 Pro 10-Core 2.4 GHz")
    private String name;

    @Schema(description = "物理核心数", example = "10")
    private Integer physicalCores;

    @Schema(description = "逻辑核心数", example = "10")
    private Integer logicalCores;

    @Schema(description = "CPU 整体使用率（%）", example = "12.34")
    private Double usage;

    @Schema(description = "用户态使用率（%）", example = "8.21")
    private Double userUsage;

    @Schema(description = "系统态使用率（%）", example = "4.13")
    private Double sysUsage;

    @Schema(description = "等待 IO 占比（%）", example = "0.00")
    private Double waitUsage;
}

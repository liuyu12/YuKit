package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 服务器监控总览：CPU / JVM / 内存 / 磁盘 / 操作系统
 */
@Data
@Schema(description = "管理后台 - 服务器监控总览")
public class ServerInfoRespVO {

    @Schema(description = "CPU 信息")
    private CpuRespVO cpu;

    @Schema(description = "JVM 信息")
    private JvmRespVO jvm;

    @Schema(description = "物理内存信息")
    private MemRespVO mem;

    @Schema(description = "磁盘分区列表")
    private List<DiskRespVO> disks;

    @Schema(description = "操作系统信息")
    private SysRespVO sys;
}

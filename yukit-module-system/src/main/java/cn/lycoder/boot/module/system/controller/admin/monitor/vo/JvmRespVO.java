package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * JVM 监控信息
 * <p>
 * 来源 JDK {@code Runtime}/{@code ManagementFactory}，无需额外依赖。
 */
@Data
@Schema(description = "管理后台 - JVM 监控信息")
public class JvmRespVO {

    @Schema(description = "JVM 名称", example = "OpenJDK 64-Bit Server VM")
    private String name;

    @Schema(description = "JVM 版本", example = "21.0.12+8")
    private String version;

    @Schema(description = "JVM 家目录", example = "/Users/liuyu/Library/Java/...")
    private String home;

    @Schema(description = "启动时间", example = "2026-11-24 15:30:00")
    private String startTime;

    @Schema(description = "已运行时长", example = "0天0时12分30秒")
    private String runTime;

    @Schema(description = "JVM 总内存（MB）", example = "512.00")
    private Double total;

    @Schema(description = "JVM 最大可用内存（MB）", example = "4096.00")
    private Double max;

    @Schema(description = "JVM 已用内存（MB）", example = "256.00")
    private Double used;

    @Schema(description = "JVM 空闲内存（MB）", example = "256.00")
    private Double free;

    @Schema(description = "JVM 内存使用率（%）", example = "50.00")
    private Double usage;
}

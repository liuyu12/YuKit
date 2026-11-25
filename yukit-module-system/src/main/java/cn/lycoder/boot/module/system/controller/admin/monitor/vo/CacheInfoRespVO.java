package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Redis 缓存监控信息
 * <p>
 * 数据来源 Redis {@code INFO} 命令（server/memory/stats 段）+ {@code DBSIZE}，
 * 命令统计取 INFO commandstats 段按调用次数 Top N。
 */
@Data
@Schema(description = "管理后台 - Redis 缓存监控信息")
public class CacheInfoRespVO {

    @Schema(description = "Redis 版本", example = "6.2.14")
    private String redisVersion;

    @Schema(description = "运行模式", example = "standalone")
    private String mode;

    @Schema(description = "监听端口", example = "6379")
    private Integer port;

    @Schema(description = "已运行天数", example = "12")
    private Long uptimeDays;

    @Schema(description = "当前连接客户端数", example = "8")
    private Integer connectedClients;

    @Schema(description = "已用内存（可读格式）", example = "1.25M")
    private String usedMemoryHuman;

    @Schema(description = "内存峰值（可读格式）", example = "2.10M")
    private String usedMemoryPeakHuman;

    @Schema(description = "最大内存限制（可读格式，未限制为空）", example = "256M")
    private String maxMemoryHuman;

    @Schema(description = "内存利用率（%），未设置 maxmemory 时为 null", example = "0.49")
    private Double memoryUsage;

    @Schema(description = "当前库 key 总数", example = "123")
    private Long dbSize;

    @Schema(description = "缓存命中次数", example = "456")
    private Long keyspaceHits;

    @Schema(description = "缓存未命中次数", example = "7")
    private Long keyspaceMisses;

    @Schema(description = "缓存命中率（%）", example = "98.49")
    private Double hitRate;

    @Schema(description = "每秒执行命令数", example = "15")
    private Long instantaneousOpsPerSec;

    @Schema(description = "累计被拒绝的非法连接数", example = "0")
    private Long rejectedConnections;

    @Schema(description = "累计被淘汰（LRU 等策略）的 key 数", example = "0")
    private Long evictedKeys;

    @Schema(description = "命令调用统计 Top 10")
    private List<CacheCommandStatRespVO> commandStats;
}

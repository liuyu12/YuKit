package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Redis 命令调用统计（INFO commandstats 段单条）
 */
@Data
@Schema(description = "管理后台 - Redis 命令调用统计")
public class CacheCommandStatRespVO {

    @Schema(description = "命令名称", example = "get")
    private String name;

    @Schema(description = "累计调用次数", example = "1024")
    private Long calls;

    @Schema(description = "占总调用比例（%）", example = "12.34")
    private Double percent;
}

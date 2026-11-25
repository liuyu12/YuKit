package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Redis 缓存 key 详情（含值预览）
 * <p>
 * 仅 string 类型返回值预览，其他类型给出说明，避免大集合全量拉取。
 */
@Data
@Schema(description = "管理后台 - Redis 缓存 key 详情")
public class CacheValueRespVO {

    @Schema(description = "缓存 key", example = "yukit:login:fail:ry")
    private String key;

    @Schema(description = "值类型", example = "string")
    private String type;

    @Schema(description = "剩余过期时间（秒），-1 永不过期，-2 已不存在", example = "590")
    private Long expire;

    @Schema(description = "值预览（string 类型截断展示，其他类型为说明文字）", example = "3")
    private String value;
}

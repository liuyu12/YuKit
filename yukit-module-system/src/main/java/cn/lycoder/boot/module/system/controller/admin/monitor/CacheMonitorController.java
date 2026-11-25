package cn.lycoder.boot.module.system.controller.admin.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheInfoRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheKeyRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheValueRespVO;
import cn.lycoder.boot.module.system.service.monitor.CacheMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 缓存监控：Redis 运行指标与 key 运维（对齐菜单 113 缓存监控 / 114 缓存列表）
 */
@Tag(name = "缓存监控")
@RestController
@RequestMapping("/monitor/cache")
@RequiredArgsConstructor
@Validated
public class CacheMonitorController {

    /**
     * key 列表默认返回条数
     */
    private static final int DEFAULT_KEY_LIMIT = 50;

    private final CacheMonitorService cacheMonitorService;

    /**
     * 获取 Redis 运行信息
     */
    @Operation(summary = "获取 Redis 运行信息")
    @GetMapping("/info")
    @SaCheckPermission("monitor:cache:list")
    public Result<CacheInfoRespVO> getCacheInfo() {
        return Result.success(cacheMonitorService.getCacheInfo());
    }

    /**
     * 按模式扫描缓存 key
     */
    @Operation(summary = "按模式扫描缓存 key")
    @GetMapping("/keys")
    @SaCheckPermission("monitor:cache:list")
    public Result<List<CacheKeyRespVO>> getCacheKeys(
            @Parameter(description = "glob 模式，如 yukit:*，为空查全部") @RequestParam(value = "pattern", required = false) String pattern,
            @Parameter(description = "返回条数上限") @RequestParam(value = "limit", defaultValue = "50")
            @Min(value = 1, message = "返回条数最小为 1") @Max(value = 200, message = "返回条数最大为 200") Integer limit) {
        int safeLimit = limit == null ? DEFAULT_KEY_LIMIT : limit;
        return Result.success(cacheMonitorService.getCacheKeys(pattern, safeLimit));
    }

    /**
     * 查看单个 key 详情
     */
    @Operation(summary = "查看单个缓存 key 详情")
    @GetMapping("/value")
    @SaCheckPermission("monitor:cache:list")
    public Result<CacheValueRespVO> getCacheValue(
            @Parameter(description = "缓存 key") @RequestParam("key") String key) {
        return Result.success(cacheMonitorService.getCacheValue(key));
    }

    /**
     * 删除单个缓存 key
     */
    @Operation(summary = "删除单个缓存 key")
    @OperationLog(module = "缓存监控", name = "删除缓存 key", type = OperationType.DELETE)
    @DeleteMapping("/keys/{key}")
    @SaCheckPermission("monitor:cache:list")
    public Result<Boolean> deleteCacheKey(
            @Parameter(description = "缓存 key") @PathVariable("key") String key) {
        return Result.success(cacheMonitorService.deleteCacheKey(key));
    }
}

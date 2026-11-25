package cn.lycoder.boot.module.system.service.monitor;

import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheInfoRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheKeyRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheValueRespVO;

import java.util.List;

/**
 * 缓存监控 Service：Redis 运行指标与 key 运维查询
 */
public interface CacheMonitorService {

    /**
     * 获取 Redis 运行信息（INFO + DBSIZE + 命令统计 Top N）
     */
    CacheInfoRespVO getCacheInfo();

    /**
     * 按 glob 模式扫描缓存 key（SCAN 增量遍历，限量返回）
     *
     * @param pattern glob 模式，如 {@code yukit:*}，为空默认全部
     * @param limit   返回条数上限
     */
    List<CacheKeyRespVO> getCacheKeys(String pattern, int limit);

    /**
     * 获取单个 key 详情（类型 / TTL / 值预览）
     */
    CacheValueRespVO getCacheValue(String key);

    /**
     * 删除单个缓存 key
     *
     * @return key 是否存在并被删除
     */
    boolean deleteCacheKey(String key);
}

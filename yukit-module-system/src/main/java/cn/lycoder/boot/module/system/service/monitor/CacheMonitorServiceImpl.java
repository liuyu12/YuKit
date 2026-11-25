package cn.lycoder.boot.module.system.service.monitor;

import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheCommandStatRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheInfoRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheKeyRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheValueRespVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * 缓存监控实现：基于主连接工厂（业务库）执行 INFO / DBSIZE / SCAN
 * <p>
 * key 遍历用 SCAN 增量命令而非 KEYS，避免大库阻塞 Redis；值预览仅支持
 * string 类型并截断，其余类型只回说明，防止大集合全量拉取。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheMonitorServiceImpl implements CacheMonitorService {

    /**
     * 未指定模式时匹配全部 key
     */
    private static final String DEFAULT_PATTERN = "*";
    /**
     * 每次 SCAN 的服务端批大小
     */
    private static final int SCAN_BATCH_SIZE = 500;
    /**
     * key 列表返回条数上限
     */
    private static final int MAX_KEY_LIMIT = 200;
    /**
     * string 值预览的最大长度
     */
    private static final int MAX_VALUE_LENGTH = 512;
    /**
     * 命令统计展示条数（按调用次数取 Top N）
     */
    private static final int COMMAND_STAT_TOP = 10;
    /**
     * INFO commandstats 段的 key 前缀
     */
    private static final String CMDSTAT_PREFIX = "cmdstat_";
    /**
     * INFO 命令统计段名（Lettuce 全量 INFO 会丢弃 cmdstat_* 行，需单独取段）
     */
    private static final String INFO_SECTION_COMMANDSTATS = "commandstats";
    /**
     * commandstats 值里的调用次数段
     */
    private static final String CALLS_SEGMENT_PREFIX = "calls=";
    /**
     * 百分比换算基数
     */
    private static final double PERCENT_BASE = 100.0D;
    /**
     * 预览截断省略号
     */
    private static final String ELLIPSIS = "...";
    /**
     * 非 string 类型的预览说明
     */
    private static final String NON_STRING_HINT_TEMPLATE = "（%s 类型，暂不支持值预览）";
    /**
     * 未知类型占位
     */
    private static final String TYPE_UNKNOWN = "none";
    /**
     * key 不存在的说明
     */
    private static final String KEY_ABSENT_HINT = "（key 不存在）";

    private final StringRedisTemplate redisTemplate;

    @Override
    public CacheInfoRespVO getCacheInfo() {
        Properties info = redisTemplate.execute((RedisCallback<Properties>) connection -> connection.serverCommands().info());
        Properties commandStats = redisTemplate.execute((RedisCallback<Properties>) connection ->
                connection.serverCommands().info(INFO_SECTION_COMMANDSTATS));
        Long dbSize = redisTemplate.execute((RedisCallback<Long>) connection -> connection.serverCommands().dbSize());
        CacheInfoRespVO vo = new CacheInfoRespVO();
        if (info == null) {
            vo.setDbSize(dbSize);
            return vo;
        }
        fillServerInfo(vo, info);
        fillMemoryInfo(vo, info);
        fillStatsInfo(vo, info);
        fillCommandStats(vo, commandStats);
        vo.setDbSize(dbSize);
        return vo;
    }

    private void fillServerInfo(CacheInfoRespVO vo, Properties info) {
        vo.setRedisVersion(info.getProperty("redis_version"));
        // Redis 6/7 的 INFO 字段名为 redis_mode（standalone|cluster|sentinel）
        vo.setMode(info.getProperty("redis_mode"));
        vo.setPort(intProp(info, "tcp_port"));
        vo.setUptimeDays(longProp(info, "uptime_in_days"));
        vo.setConnectedClients(intProp(info, "connected_clients"));
    }

    private void fillMemoryInfo(CacheInfoRespVO vo, Properties info) {
        vo.setUsedMemoryHuman(info.getProperty("used_memory_human"));
        vo.setUsedMemoryPeakHuman(info.getProperty("used_memory_peak_human"));
        vo.setMaxMemoryHuman(info.getProperty("max_memory_human"));
        Long usedMemory = longProp(info, "used_memory");
        Long maxMemory = longProp(info, "max_memory");
        // maxmemory 未限制（0）或缺数据时不计算利用率
        if (usedMemory != null && maxMemory != null && maxMemory > 0L) {
            vo.setMemoryUsage(round(usedMemory / (double) maxMemory * PERCENT_BASE));
        }
    }

    private void fillStatsInfo(CacheInfoRespVO vo, Properties info) {
        Long hits = longProp(info, "keyspace_hits");
        Long misses = longProp(info, "keyspace_misses");
        vo.setKeyspaceHits(hits);
        vo.setKeyspaceMisses(misses);
        if (hits != null && misses != null && hits + misses > 0L) {
            vo.setHitRate(round(hits / (double) (hits + misses) * PERCENT_BASE));
        }
        vo.setInstantaneousOpsPerSec(longProp(info, "instantaneous_ops_per_sec"));
        vo.setRejectedConnections(longProp(info, "rejected_connections"));
        vo.setEvictedKeys(longProp(info, "evicted_keys"));
    }

    /**
     * 解析 INFO commandstats 段：形如 cmdstat_get -> calls=100,usec=200,usec_per_call=2.00
     */
    private void fillCommandStats(CacheInfoRespVO vo, Properties info) {
        if (info == null) {
            return;
        }
        List<CacheCommandStatRespVO> stats = new ArrayList<>();
        long totalCalls = 0L;
        for (String name : info.stringPropertyNames()) {
            if (!name.startsWith(CMDSTAT_PREFIX)) {
                continue;
            }
            CacheCommandStatRespVO stat = new CacheCommandStatRespVO();
            stat.setName(name.substring(CMDSTAT_PREFIX.length()));
            stat.setCalls(parseCalls(info.getProperty(name)));
            totalCalls += stat.getCalls();
            stats.add(stat);
        }
        long finalTotalCalls = totalCalls;
        if (finalTotalCalls > 0L) {
            stats.forEach(stat -> stat.setPercent(round(stat.getCalls() / (double) finalTotalCalls * PERCENT_BASE)));
        }
        stats.sort(Comparator.comparing(CacheCommandStatRespVO::getCalls).reversed());
        vo.setCommandStats(stats.size() > COMMAND_STAT_TOP ? stats.subList(0, COMMAND_STAT_TOP) : stats);
    }

    private long parseCalls(String statValue) {
        if (!StringUtils.hasText(statValue)) {
            return 0L;
        }
        for (String segment : statValue.split(",")) {
            if (segment.startsWith(CALLS_SEGMENT_PREFIX)) {
                try {
                    return Long.parseLong(segment.substring(CALLS_SEGMENT_PREFIX.length()));
                } catch (NumberFormatException e) {
                    return 0L;
                }
            }
        }
        return 0L;
    }

    @Override
    public List<CacheKeyRespVO> getCacheKeys(String pattern, int limit) {
        String match = StringUtils.hasText(pattern) ? pattern : DEFAULT_PATTERN;
        int capped = Math.min(Math.max(limit, 1), MAX_KEY_LIMIT);
        return redisTemplate.execute((RedisCallback<List<CacheKeyRespVO>>) connection -> {
            List<CacheKeyRespVO> keys = new ArrayList<>();
            ScanOptions options = ScanOptions.scanOptions().match(match).count(SCAN_BATCH_SIZE).build();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext() && keys.size() < capped) {
                    byte[] rawKey = cursor.next();
                    keys.add(convertKey(connection, rawKey));
                }
            }
            return keys;
        });
    }

    private CacheKeyRespVO convertKey(RedisConnection connection, byte[] rawKey) {
        CacheKeyRespVO vo = new CacheKeyRespVO();
        vo.setKey(new String(rawKey, StandardCharsets.UTF_8));
        DataType type = connection.keyCommands().type(rawKey);
        vo.setType(type == null ? TYPE_UNKNOWN : type.code());
        vo.setExpire(connection.keyCommands().ttl(rawKey));
        return vo;
    }

    @Override
    public CacheValueRespVO getCacheValue(String key) {
        return redisTemplate.execute((RedisCallback<CacheValueRespVO>) connection -> {
            byte[] rawKey = key.getBytes(StandardCharsets.UTF_8);
            DataType type = connection.keyCommands().type(rawKey);
            CacheValueRespVO vo = new CacheValueRespVO();
            vo.setKey(key);
            vo.setType(type == null ? TYPE_UNKNOWN : type.code());
            vo.setExpire(connection.keyCommands().ttl(rawKey));
            vo.setValue(readValuePreview(connection, type, rawKey));
            return vo;
        });
    }

    /**
     * 值预览：仅 string 拉取并截断，集合类型只回说明（避免大 key 全量传输）
     */
    private String readValuePreview(RedisConnection connection, DataType type, byte[] rawKey) {
        if (type == null || type == DataType.NONE) {
            return KEY_ABSENT_HINT;
        }
        if (type != DataType.STRING) {
            return String.format(NON_STRING_HINT_TEMPLATE, type.code());
        }
        byte[] value = connection.stringCommands().get(rawKey);
        if (value == null) {
            return "";
        }
        String text = new String(value, StandardCharsets.UTF_8);
        return text.length() > MAX_VALUE_LENGTH ? text.substring(0, MAX_VALUE_LENGTH) + ELLIPSIS : text;
    }

    @Override
    public boolean deleteCacheKey(String key) {
        Long removed = redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.keyCommands().del(key.getBytes(StandardCharsets.UTF_8)));
        return removed != null && removed > 0L;
    }

    private Integer intProp(Properties info, String name) {
        Long value = longProp(info, name);
        return value == null ? null : value.intValue();
    }

    private Long longProp(Properties info, String name) {
        String value = info.getProperty(name);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }
}

package cn.lycoder.boot.module.system.service.monitor;

import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheCommandStatRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheInfoRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheKeyRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CacheValueRespVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CacheMonitorServiceImpl} 单元测试：纯 Mockito，不起 Spring 上下文
 */
@ExtendWith(MockitoExtension.class)
class CacheMonitorServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private RedisConnection connection;
    @Mock
    private RedisServerCommands serverCommands;
    @Mock
    private RedisKeyCommands keyCommands;
    @Mock
    private RedisStringCommands stringCommands;

    private CacheMonitorServiceImpl cacheMonitorService;

    @BeforeEach
    void setUp() {
        cacheMonitorService = new CacheMonitorServiceImpl(redisTemplate);
    }

    /**
     * 让 execute(RedisCallback) 直接在 mock 连接上回放回调
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubExecute() {
        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation ->
                ((RedisCallback) invocation.getArgument(0)).doInRedis(connection));
    }

    @Test
    void getCacheInfoShouldParseInfoAndComputeRates() {
        Properties info = new Properties();
        info.setProperty("redis_version", "6.2.14");
        info.setProperty("redis_mode", "standalone");
        info.setProperty("tcp_port", "6379");
        info.setProperty("uptime_in_days", "12");
        info.setProperty("connected_clients", "8");
        info.setProperty("used_memory", "1000");
        info.setProperty("max_memory", "4000");
        info.setProperty("used_memory_human", "1.00K");
        info.setProperty("keyspace_hits", "99");
        info.setProperty("keyspace_misses", "1");
        info.setProperty("instantaneous_ops_per_sec", "15");
        // 全量 INFO 不含 cmdstat（Lettuce 丢弃），命令统计单独取段
        Properties commandStats = new Properties();
        commandStats.setProperty("cmdstat_get", "calls=100,usec=200,usec_per_call=2.00");
        commandStats.setProperty("cmdstat_set", "calls=50,usec=100,usec_per_call=2.00");
        when(serverCommands.info()).thenReturn(info);
        when(serverCommands.info("commandstats")).thenReturn(commandStats);
        when(serverCommands.dbSize()).thenReturn(5L);
        when(connection.serverCommands()).thenReturn(serverCommands);
        stubExecute();

        CacheInfoRespVO vo = cacheMonitorService.getCacheInfo();

        assertThat(vo.getRedisVersion()).isEqualTo("6.2.14");
        assertThat(vo.getMode()).isEqualTo("standalone");
        assertThat(vo.getPort()).isEqualTo(6379);
        assertThat(vo.getDbSize()).isEqualTo(5L);
        assertThat(vo.getMemoryUsage()).isEqualTo(25.0D);
        assertThat(vo.getHitRate()).isEqualTo(99.0D);
        List<CacheCommandStatRespVO> stats = vo.getCommandStats();
        assertThat(stats).hasSize(2);
        // 按调用次数降序，占比正确
        assertThat(stats.get(0).getName()).isEqualTo("get");
        assertThat(stats.get(0).getPercent()).isEqualTo(66.67D);
        assertThat(stats.get(1).getName()).isEqualTo("set");
    }

    @Test
    void getCacheKeysShouldScanAndConvertTypeAndTtl() {
        stubExecute();
        when(connection.keyCommands()).thenReturn(keyCommands);
        Cursor<byte[]> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("key:a".getBytes(StandardCharsets.UTF_8), "key:b".getBytes(StandardCharsets.UTF_8));
        when(keyCommands.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(keyCommands.type(any())).thenReturn(DataType.STRING);
        when(keyCommands.ttl(any())).thenReturn(100L);

        List<CacheKeyRespVO> keys = cacheMonitorService.getCacheKeys("key:*", 10);

        assertThat(keys).hasSize(2);
        assertThat(keys.get(0).getKey()).isEqualTo("key:a");
        assertThat(keys.get(0).getType()).isEqualTo("string");
        assertThat(keys.get(0).getExpire()).isEqualTo(100L);
    }

    @Test
    void getCacheValueShouldTruncateLongStringValue() {
        stubExecute();
        when(connection.keyCommands()).thenReturn(keyCommands);
        when(connection.stringCommands()).thenReturn(stringCommands);
        when(keyCommands.type(any())).thenReturn(DataType.STRING);
        when(keyCommands.ttl(any())).thenReturn(-1L);
        String longValue = "x".repeat(600);
        when(stringCommands.get(any())).thenReturn(longValue.getBytes(StandardCharsets.UTF_8));

        CacheValueRespVO vo = cacheMonitorService.getCacheValue("key:a");

        assertThat(vo.getType()).isEqualTo("string");
        assertThat(vo.getValue()).hasSize(515).endsWith("...");
    }

    @Test
    void getCacheValueShouldExplainNonStringType() {
        stubExecute();
        when(connection.keyCommands()).thenReturn(keyCommands);
        when(keyCommands.type(any())).thenReturn(DataType.HASH);
        when(keyCommands.ttl(any())).thenReturn(60L);

        CacheValueRespVO vo = cacheMonitorService.getCacheValue("key:h");

        assertThat(vo.getType()).isEqualTo("hash");
        assertThat(vo.getValue()).contains("hash");
    }

    @Test
    void deleteCacheKeyShouldReturnWhetherKeyExisted() {
        stubExecute();
        when(connection.keyCommands()).thenReturn(keyCommands);
        when(keyCommands.del(any())).thenReturn(1L, 0L);

        assertThat(cacheMonitorService.deleteCacheKey("key:a")).isTrue();
        assertThat(cacheMonitorService.deleteCacheKey("key:missing")).isFalse();
    }
}

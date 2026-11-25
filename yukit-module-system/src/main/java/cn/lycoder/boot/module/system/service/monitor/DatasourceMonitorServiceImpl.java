package cn.lycoder.boot.module.system.service.monitor;

import cn.lycoder.boot.module.system.controller.admin.monitor.vo.DatasourceRespVO;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * 数据源监控实现
 * <p>
 * DB 元信息借一条连接读 {@link DatabaseMetaData}（读完即归还）；连接池指标
 * 在数据源为 HikariCP 时读取，其他实现只返回元信息、池字段留空。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasourceMonitorServiceImpl implements DatasourceMonitorService {

    /**
     * 驱动名与版本号之间的连接符
     */
    private static final String DRIVER_VERSION_JOINER = " ";

    private final DataSource dataSource;

    @Override
    public DatasourceRespVO getDatasourceInfo() {
        DatasourceRespVO vo = new DatasourceRespVO();
        fillMetaData(vo);
        fillPoolStats(vo);
        return vo;
    }

    private void fillMetaData(DatasourceRespVO vo) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            vo.setProduct(metaData.getDatabaseProductName());
            vo.setProductVersion(metaData.getDatabaseProductVersion());
            vo.setDriver(metaData.getDriverName() + DRIVER_VERSION_JOINER + metaData.getDriverVersion());
            vo.setUrl(metaData.getURL());
            vo.setUsername(metaData.getUserName());
        } catch (SQLException e) {
            // 监控旁路：读不到元信息不抛错，字段留空
            log.warn("[fillMetaData] 读取数据库元信息失败", e);
        }
    }

    private void fillPoolStats(DatasourceRespVO vo) {
        if (!(dataSource instanceof HikariDataSource hikari)) {
            return;
        }
        vo.setPoolName(hikari.getPoolName());
        vo.setMaximumPoolSize(hikari.getMaximumPoolSize());
        vo.setMinimumIdle(hikari.getMinimumIdle());
        vo.setConnectionTimeoutMs(hikari.getConnectionTimeout());
        vo.setIdleTimeoutMs(hikari.getIdleTimeout());
        vo.setMaxLifetimeMs(hikari.getMaxLifetime());
        // 池未启动（如懒初始化）时 MXBean 为 null，仅保留配置项
        HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
        if (pool != null) {
            vo.setTotalConnections(pool.getTotalConnections());
            vo.setIdleConnections(pool.getIdleConnections());
            vo.setActiveConnections(pool.getActiveConnections());
            vo.setThreadsAwaitingConnection(pool.getThreadsAwaitingConnection());
        }
    }
}

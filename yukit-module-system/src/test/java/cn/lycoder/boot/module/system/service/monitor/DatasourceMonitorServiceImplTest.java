package cn.lycoder.boot.module.system.service.monitor;

import cn.lycoder.boot.module.system.controller.admin.monitor.vo.DatasourceRespVO;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link DatasourceMonitorServiceImpl} 单元测试：纯 Mockito，不起 Spring 上下文
 */
@ExtendWith(MockitoExtension.class)
class DatasourceMonitorServiceImplTest {

    @Test
    void shouldFillMetadataAndHikariPoolStats() throws SQLException {
        HikariDataSource dataSource = mock(HikariDataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        HikariPoolMXBean pool = mock(HikariPoolMXBean.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("8.0.40");
        when(metaData.getDriverName()).thenReturn("MySQL Connector-J");
        when(metaData.getDriverVersion()).thenReturn("9.1.0");
        when(metaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/yukit");
        when(metaData.getUserName()).thenReturn("root");
        when(dataSource.getPoolName()).thenReturn("HikariPool-1");
        when(dataSource.getMaximumPoolSize()).thenReturn(20);
        when(dataSource.getMinimumIdle()).thenReturn(5);
        when(dataSource.getConnectionTimeout()).thenReturn(30000L);
        when(dataSource.getIdleTimeout()).thenReturn(600000L);
        when(dataSource.getMaxLifetime()).thenReturn(1800000L);
        when(dataSource.getHikariPoolMXBean()).thenReturn(pool);
        when(pool.getTotalConnections()).thenReturn(6);
        when(pool.getIdleConnections()).thenReturn(5);
        when(pool.getActiveConnections()).thenReturn(1);
        when(pool.getThreadsAwaitingConnection()).thenReturn(0);

        DatasourceRespVO vo = new DatasourceMonitorServiceImpl(dataSource).getDatasourceInfo();

        assertThat(vo.getProduct()).isEqualTo("MySQL");
        assertThat(vo.getProductVersion()).isEqualTo("8.0.40");
        assertThat(vo.getDriver()).isEqualTo("MySQL Connector-J 9.1.0");
        assertThat(vo.getUrl()).isEqualTo("jdbc:mysql://localhost:3306/yukit");
        assertThat(vo.getPoolName()).isEqualTo("HikariPool-1");
        assertThat(vo.getTotalConnections()).isEqualTo(6);
        assertThat(vo.getActiveConnections()).isEqualTo(1);
        assertThat(vo.getMaxLifetimeMs()).isEqualTo(1800000L);
    }

    @Test
    void shouldSkipPoolStatsForNonHikariDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("H2");

        DatasourceRespVO vo = new DatasourceMonitorServiceImpl(dataSource).getDatasourceInfo();

        assertThat(vo.getProduct()).isEqualTo("H2");
        assertThat(vo.getPoolName()).isNull();
        assertThat(vo.getTotalConnections()).isNull();
    }

    @Test
    void shouldDegradeGracefullyWhenMetadataLookupFails() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("database down"));

        DatasourceMonitorService service = new DatasourceMonitorServiceImpl(dataSource);

        assertThatNoException().isThrownBy(service::getDatasourceInfo);
        assertThat(service.getDatasourceInfo().getProduct()).isNull();
    }
}

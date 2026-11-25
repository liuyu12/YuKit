package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据源（连接池）监控信息
 * <p>
 * DB 元信息取自 JDBC {@code DatabaseMetaData}；连接池指标取自 HikariCP
 * {@code HikariPoolMXBean}（非 Hikari 实现时池字段为 null）。
 */
@Data
@Schema(description = "管理后台 - 数据源监控信息")
public class DatasourceRespVO {

    @Schema(description = "数据库产品名", example = "MySQL")
    private String product;

    @Schema(description = "数据库版本", example = "8.0.40")
    private String productVersion;

    @Schema(description = "JDBC 驱动", example = "MySQL Connector-J 9.x")
    private String driver;

    @Schema(description = "连接地址", example = "jdbc:mysql://localhost:3306/yukit")
    private String url;

    @Schema(description = "连接账号", example = "root")
    private String username;

    @Schema(description = "连接池名称", example = "HikariPool-1")
    private String poolName;

    @Schema(description = "连接池最大连接数", example = "20")
    private Integer maximumPoolSize;

    @Schema(description = "连接池最小空闲连接数", example = "5")
    private Integer minimumIdle;

    @Schema(description = "当前连接总数", example = "6")
    private Integer totalConnections;

    @Schema(description = "空闲连接数", example = "5")
    private Integer idleConnections;

    @Schema(description = "活跃（已借出）连接数", example = "1")
    private Integer activeConnections;

    @Schema(description = "等待获取连接的线程数", example = "0")
    private Integer threadsAwaitingConnection;

    @Schema(description = "获取连接超时时间（毫秒）", example = "30000")
    private Long connectionTimeoutMs;

    @Schema(description = "空闲连接超时回收时间（毫秒）", example = "600000")
    private Long idleTimeoutMs;

    @Schema(description = "连接最大存活时间（毫秒）", example = "1800000")
    private Long maxLifetimeMs;
}

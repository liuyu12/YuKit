package cn.lycoder.boot.module.system.service.monitor;

import cn.lycoder.boot.module.system.controller.admin.monitor.vo.ServerInfoRespVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * ServerMonitorServiceImpl 单测：OSHI 指标无法也无需 mock，
 * 直接真实采集断言组装结构完整、数值落在合理区间（本机运行必有值）。
 */
class ServerMonitorServiceImplTest {

    private ServerMonitorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ServerMonitorServiceImpl();
    }

    @Test
    @DisplayName("getServerInfo 五大区块齐全且指标区间合理")
    void getServerInfoShouldAssembleAllSections() {
        ServerInfoRespVO vo = service.getServerInfo();

        assertThat(vo.getCpu()).isNotNull();
        assertThat(vo.getJvm()).isNotNull();
        assertThat(vo.getMem()).isNotNull();
        assertThat(vo.getSys()).isNotNull();
        assertThat(vo.getDisks()).isNotNull();

        // CPU：任何能跑 JVM 的机器核数必 >0，使用率是百分比
        assertThat(vo.getCpu().getPhysicalCores()).isPositive();
        assertThat(vo.getCpu().getLogicalCores()).isPositive();
        assertThat(vo.getCpu().getUsage()).isBetween(0.0, 100.0);
        assertThat(vo.getCpu().getName()).isNotBlank();

        // 物理内存：总量/已用非负，使用率百分比
        assertThat(vo.getMem().getTotal()).isPositive();
        assertThat(vo.getMem().getUsed()).isNotNegative();
        assertThat(vo.getMem().getUsage()).isBetween(0.0, 100.0);

        // JVM：max 一定大于 0，used ≤ total ≤ max 的单调关系
        assertThat(vo.getJvm().getMax()).isPositive();
        assertThat(vo.getJvm().getUsed()).isNotNegative();
        assertThat(vo.getJvm().getTotal()).isLessThanOrEqualTo(vo.getJvm().getMax());
        assertThat(vo.getJvm().getUsage()).isBetween(0.0, 100.0);
        assertThat(vo.getJvm().getStartTime()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        assertThat(vo.getJvm().getRunTime()).matches("\\d+天\\d+时\\d+分\\d+秒");

        // 操作系统：family 与架构必有值；主机名/IP 解析失败也有兜底值
        assertThat(vo.getSys().getOsName()).isNotBlank();
        assertThat(vo.getSys().getOsArch()).isNotBlank();
        assertThat(vo.getSys().getComputerIp()).isNotBlank();
    }

    @Test
    @DisplayName("磁盘列表各分区用量字段自洽（used = total - free，允许舍入误差）")
    void disksShouldBeSelfConsistent() {
        ServerInfoRespVO vo = service.getServerInfo();

        vo.getDisks().forEach(disk -> {
            assertThat(disk.getDirName()).isNotBlank();
            assertThat(disk.getTotal()).isNotNegative();
            assertThat(disk.getUsed()).isNotNegative();
            assertThat(disk.getFree()).isNotNegative();
            // GB 保留两位小数后的舍入容差
            assertThat(disk.getUsed() + disk.getFree())
                    .isCloseTo(disk.getTotal(), within(0.05));
            assertThat(disk.getUsage()).isBetween(0.0, 100.0);
        });
    }
}

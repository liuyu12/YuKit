package cn.lycoder.boot.module.system.service.monitor;

import cn.lycoder.boot.module.system.controller.admin.monitor.vo.CpuRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.DiskRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.JvmRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.MemRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.ServerInfoRespVO;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.SysRespVO;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 服务器监控 Service 实现：基于 OSHI + JDK 内置 MBean 采集系统/硬件/JVM 指标
 * <p>
 * CPU 使用率采用「两次 tick 差值」法，间隔 {@link #CPU_SAMPLE_INTERVAL_MS} 毫秒，
 * 返回窗口期内的平均负载，避免单点采样抖动且兼容各平台。
 */
@Service
@Slf4j
public class ServerMonitorServiceImpl implements ServerMonitorService {

    /**
     * CPU 采样窗口（毫秒），兼顾准确性与接口响应延迟
     */
    private static final long CPU_SAMPLE_INTERVAL_MS = 1000L;
    /**
     * 字节 → KB/MB/GB 换算基数
     */
    private static final double BYTES_PER_KB = 1024.0;
    private static final double BYTES_PER_MB = BYTES_PER_KB * 1024.0;
    private static final double BYTES_PER_GB = BYTES_PER_MB * 1024.0;
    /**
     * 比例 → 百分数换算
     */
    private static final double PERCENT_BASE = 100.0;
    /**
     * 启动时间格式化（监控页展示用）
     */
    private static final DateTimeFormatter START_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 运行时长格式化模板
     */
    private static final String RUN_TIME_PATTERN = "%d天%d时%d分%d秒";
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60L;
    private static final long SECONDS_PER_DAY = SECONDS_PER_HOUR * 24L;

    /**
     * OSHI SystemInfo 是线程安全的可复用句柄，内部维护硬件缓存
     */
    private final SystemInfo systemInfo = new SystemInfo();

    @Override
    public ServerInfoRespVO getServerInfo() {
        ServerInfoRespVO vo = new ServerInfoRespVO();
        vo.setCpu(buildCpu());
        vo.setJvm(buildJvm());
        vo.setMem(buildMem());
        vo.setDisks(buildDisks());
        vo.setSys(buildSys());
        return vo;
    }

    // ---------- CPU ----------

    private CpuRespVO buildCpu() {
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        CpuRespVO vo = new CpuRespVO();
        vo.setName(processor.getProcessorIdentifier().getName().trim());
        vo.setPhysicalCores(processor.getPhysicalProcessorCount());
        vo.setLogicalCores(processor.getLogicalProcessorCount());

        // 两次 tick 差值采样，得到 [t0, t1] 区间内的负载分布
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(CPU_SAMPLE_INTERVAL_MS);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long total = 0L;
        for (long tick : ticks) {
            total += tick;
        }
        if (total <= 0L) {
            vo.setUsage(0.0);
            vo.setUserUsage(0.0);
            vo.setSysUsage(0.0);
            vo.setWaitUsage(0.0);
            return vo;
        }
        double active = total - delta(prevTicks, ticks, CentralProcessor.TickType.IDLE);
        vo.setUsage(round(active / total * PERCENT_BASE));
        vo.setUserUsage(round(delta(prevTicks, ticks, CentralProcessor.TickType.USER) / (double) total * PERCENT_BASE));
        vo.setSysUsage(round(delta(prevTicks, ticks, CentralProcessor.TickType.SYSTEM) / (double) total * PERCENT_BASE));
        vo.setWaitUsage(round(delta(prevTicks, ticks, CentralProcessor.TickType.IOWAIT) / (double) total * PERCENT_BASE));
        return vo;
    }

    private static long delta(long[] prev, long[] cur, CentralProcessor.TickType type) {
        int i = type.getIndex();
        return Math.max(0L, cur[i] - prev[i]);
    }

    // ---------- 物理内存 ----------

    private MemRespVO buildMem() {
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        GlobalMemory memory = hardware.getMemory();
        MemRespVO vo = new MemRespVO();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;
        vo.setTotal(round(total / BYTES_PER_GB));
        vo.setUsed(round(used / BYTES_PER_GB));
        vo.setFree(round(available / BYTES_PER_GB));
        vo.setUsage(total > 0 ? round(used / (double) total * PERCENT_BASE) : 0.0);
        return vo;
    }

    // ---------- JVM ----------

    private JvmRespVO buildJvm() {
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        JvmRespVO vo = new JvmRespVO();
        vo.setName(runtimeBean.getVmName());
        vo.setVersion(System.getProperty("java.vm.version"));
        vo.setHome(System.getProperty("java.home"));
        Instant startInstant = Instant.ofEpochMilli(runtimeBean.getStartTime());
        vo.setStartTime(START_TIME_FORMATTER.format(LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault())));
        vo.setRunTime(formatUptime(Duration.ofMillis(runtimeBean.getUptime())));

        double max = runtime.maxMemory() / BYTES_PER_MB;
        double total = runtime.totalMemory() / BYTES_PER_MB;
        double free = runtime.freeMemory() / BYTES_PER_MB;
        double used = total - free;
        vo.setMax(round(max));
        vo.setTotal(round(total));
        vo.setFree(round(free));
        vo.setUsed(round(used));
        vo.setUsage(max > 0 ? round(used / max * PERCENT_BASE) : 0.0);
        return vo;
    }

    private static String formatUptime(Duration uptime) {
        long seconds = uptime.getSeconds();
        long days = seconds / SECONDS_PER_DAY;
        long hours = (seconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR;
        long minutes = (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        long secs = seconds % SECONDS_PER_MINUTE;
        return String.format(RUN_TIME_PATTERN, days, hours, minutes, secs);
    }

    // ---------- 磁盘 ----------

    private List<DiskRespVO> buildDisks() {
        FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();
        return fileSystem.getFileStores().stream().map(ServerMonitorServiceImpl::toDiskVo).toList();
    }

    private static DiskRespVO toDiskVo(OSFileStore store) {
        DiskRespVO vo = new DiskRespVO();
        vo.setDirName(store.getMount());
        vo.setSysTypeName(store.getType());
        vo.setTypeName(store.getDescription());
        long total = store.getTotalSpace();
        long usable = store.getUsableSpace();
        long used = total - usable;
        vo.setTotal(round(total / BYTES_PER_GB));
        vo.setFree(round(usable / BYTES_PER_GB));
        vo.setUsed(round(used / BYTES_PER_GB));
        vo.setUsage(total > 0 ? round(used / (double) total * PERCENT_BASE) : 0.0);
        return vo;
    }

    // ---------- 操作系统 ----------

    private SysRespVO buildSys() {
        OperatingSystem os = systemInfo.getOperatingSystem();
        SysRespVO vo = new SysRespVO();
        vo.setOsName(os.getFamily());
        vo.setOsVersion(os.getVersionInfo().getVersion());
        vo.setOsArch(System.getProperty("os.arch"));
        vo.setComputerName(hostName());
        vo.setComputerIp(hostIp());
        vo.setUserDir(System.getProperty("user.dir"));
        return vo;
    }

    private static String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.debug("[hostName] 解析主机名失败，回退 unknown", e);
            return "unknown";
        }
    }

    private static String hostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.debug("[hostIp] 解析主机 IP 失败，回退 127.0.0.1", e);
            return "127.0.0.1";
        }
    }

    // ---------- 工具 ----------

    /**
     * 统一保留两位小数，便于前端直接展示
     */
    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

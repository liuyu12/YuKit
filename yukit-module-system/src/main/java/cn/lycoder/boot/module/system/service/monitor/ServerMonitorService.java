package cn.lycoder.boot.module.system.service.monitor;

import cn.lycoder.boot.module.system.controller.admin.monitor.vo.ServerInfoRespVO;

/**
 * 服务器监控 Service
 */
public interface ServerMonitorService {

    /**
     * 采集服务器实时指标：CPU / JVM / 物理内存 / 磁盘 / 操作系统
     */
    ServerInfoRespVO getServerInfo();
}

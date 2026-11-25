package cn.lycoder.boot.module.system.service.monitor;

import cn.lycoder.boot.module.system.controller.admin.monitor.vo.DatasourceRespVO;

/**
 * 数据源监控 Service：DB 元信息与连接池运行状态
 */
public interface DatasourceMonitorService {

    /**
     * 获取数据源监控信息（数据库元信息 + 连接池指标）
     */
    DatasourceRespVO getDatasourceInfo();
}

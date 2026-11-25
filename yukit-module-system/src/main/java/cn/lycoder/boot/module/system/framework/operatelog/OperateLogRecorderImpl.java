package cn.lycoder.boot.module.system.framework.operatelog;

import cn.lycoder.boot.framework.common.operatelog.OperationLogRecord;
import cn.lycoder.boot.framework.common.operatelog.OperationLogRecorder;
import cn.lycoder.boot.module.system.dal.dataobject.operatelog.SystemOperateLogDO;
import cn.lycoder.boot.module.system.dal.mapper.operatelog.SystemOperateLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * {@link OperationLogRecorder} 的系统落库实现
 * <p>
 * 桥接 web starter 的采集结果与 {@code sys_operate_log} 表：字段同名，直接拷贝后插入。
 * 与 {@code DictValueProviderImpl} 一样，业务侧提供 SPI 实现、框架侧按 {@code ObjectProvider} 消费，
 * 使 web starter 不反向依赖持久层。落库异常仅告警，不影响业务（切面已兜底，这里再防一手）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperateLogRecorderImpl implements OperationLogRecorder {

    private final SystemOperateLogMapper operateLogMapper;
    /**
     * 全局时间源（yukit.zone-id 配置）
     */
    private final Clock clock;

    @Override
    public void record(OperationLogRecord record) {
        if (record == null) {
            return;
        }
        try {
            SystemOperateLogDO logDO = new SystemOperateLogDO();
            BeanUtils.copyProperties(record, logDO);
            logDO.setCreateTime(LocalDateTime.now(clock));
            operateLogMapper.insert(logDO);
        } catch (Exception e) {
            log.warn("操作日志落库失败: {}", e.getMessage());
        }
    }
}

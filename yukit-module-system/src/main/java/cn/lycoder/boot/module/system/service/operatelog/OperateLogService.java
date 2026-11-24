package cn.lycoder.boot.module.system.service.operatelog;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.operatelog.vo.OperateLogPageReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.operatelog.SystemOperateLogDO;

/**
 * 操作日志 Service
 * <p>
 * 仅负责查询与维护；写入由 {@code OperationLogRecorder} SPI 完成。
 */
public interface OperateLogService {

    /**
     * 分页查询操作日志
     */
    PageResult<SystemOperateLogDO> getOperateLogPage(OperateLogPageReqVO reqVO);

    /**
     * 获取操作日志详情
     */
    SystemOperateLogDO getOperateLog(Long id);

    /**
     * 删除单条操作日志
     */
    void deleteOperateLog(Long id);
}

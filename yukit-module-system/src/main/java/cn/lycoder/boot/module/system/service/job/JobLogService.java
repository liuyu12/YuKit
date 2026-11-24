package cn.lycoder.boot.module.system.service.job;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobLogPageReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobLogDO;

import java.util.Collection;

/**
 * 定时任务执行日志 Service
 */
public interface JobLogService {

    /**
     * 分页查询执行日志
     */
    PageResult<SystemJobLogDO> getJobLogPage(JobLogPageReqVO reqVO);

    /**
     * 查询单条执行日志
     */
    SystemJobLogDO getJobLog(Long jobLogId);

    /**
     * 调度中心/任务执行体写入日志
     */
    void createJobLog(SystemJobLogDO jobLog);

    /**
     * 删除单条执行日志
     */
    void deleteJobLog(Long jobLogId);

    /**
     * 批量删除执行日志
     */
    void deleteJobLogByIds(Collection<Long> jobLogIds);

    /**
     * 清空所有执行日志（运维场景，慎用）
     */
    void cleanJobLog();
}

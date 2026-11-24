package cn.lycoder.boot.module.system.service.job;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 定时任务 Service
 */
public interface JobService {

    /**
     * 创建任务并注册调度
     */
    Long createJob(JobCreateReqVO reqVO);

    /**
     * 更新任务并重新注册调度
     */
    void updateJob(JobUpdateReqVO reqVO);

    /**
     * 删除任务：取消调度 + 软删 DB（历史日志保留）
     */
    void deleteJob(Long jobId);

    /**
     * 批量删除
     */
    void deleteJobByIds(Collection<Long> jobIds);

    /**
     * 启停任务：status='0' 正常 / '1' 暂停
     */
    void changeStatus(Long jobId, String status);

    /**
     * 立即异步执行一次（不修改下次触发时间）
     */
    void runJob(Long jobId);

    /**
     * 查询任务详情
     */
    SystemJobDO getJob(Long jobId);

    /**
     * 分页查询
     */
    PageResult<SystemJobDO> getJobPage(JobPageReqVO reqVO);

    /**
     * 列出所有任务（启动同步/导出用）
     */
    List<SystemJobDO> getJobList();

    /**
     * 估算下次触发时间：基于 cron 表达式 + 当前时间，未启动时也可计算
     */
    LocalDateTime getNextFireTime(String cronExpression);

    /**
     * 服务启动后调用：把 DB 中所有 status='0' 的任务挂到调度器
     */
    void syncAllOnStartup();
}

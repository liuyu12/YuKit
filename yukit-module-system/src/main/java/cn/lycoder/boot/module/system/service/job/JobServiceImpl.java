package cn.lycoder.boot.module.system.service.job;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobDO;
import cn.lycoder.boot.module.system.dal.mapper.job.SystemJobMapper;
import cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants;
import cn.lycoder.boot.module.system.framework.job.annotation.JobHandler;
import cn.lycoder.boot.module.system.framework.job.core.InvokeTargetParser;
import cn.lycoder.boot.module.system.framework.job.core.JobScheduler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 定时任务 Service 实现：CRUD + 校验 + 与 JobScheduler 同步
 * <p>
 * 每次写库操作后必须同步调用 {@link JobScheduler}，保证 DB 与内存调度状态一致；
 * 服务重启时由 {@link JobStartupLoader} 拉平状态。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    /**
     * 状态字面量：0 正常
     */
    private static final String STATUS_NORMAL = SystemJobDO.STATUS_NORMAL;

    private final SystemJobMapper jobMapper;
    private final JobScheduler jobScheduler;
    private final ApplicationContext applicationContext;
    /**
     * 全局时间源（yukit.zone-id 配置）
     */
    private final Clock clock;

    @Override
    public Long createJob(JobCreateReqVO reqVO) {
        validateCron(reqVO.getCronExpression());
        validateInvokeTarget(reqVO.getInvokeTarget());
        validateNameGroupUnique(reqVO.getJobName(), reqVO.getJobGroup(), null);
        SystemJobDO job = new SystemJobDO();
        BeanUtils.copyProperties(reqVO, job);
        applyDefaults(job);
        jobMapper.insert(job);
        jobScheduler.addOrUpdateJob(job);
        return job.getJobId();
    }

    @Override
    public void updateJob(JobUpdateReqVO reqVO) {
        SystemJobDO existing = requireJob(reqVO.getJobId());
        validateCron(reqVO.getCronExpression());
        validateInvokeTarget(reqVO.getInvokeTarget());
        validateNameGroupUnique(reqVO.getJobName(), reqVO.getJobGroup(), reqVO.getJobId());
        BeanUtils.copyProperties(reqVO, existing);
        applyDefaults(existing);
        jobMapper.updateById(existing);
        jobScheduler.addOrUpdateJob(existing);
    }

    @Override
    public void deleteJob(Long jobId) {
        requireJob(jobId);
        jobMapper.deleteById(jobId);
        jobScheduler.removeJob(jobId);
    }

    @Override
    public void deleteJobByIds(Collection<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return;
        }
        jobMapper.deleteByIds(jobIds);
        jobIds.forEach(jobScheduler::removeJob);
    }

    @Override
    public void changeStatus(Long jobId, String status) {
        if (!STATUS_NORMAL.equals(status) && !SystemJobDO.STATUS_PAUSE.equals(status)) {
            throw new ServiceException(SystemErrorCodeConstants.JOB_NOT_EXISTS);
        }
        SystemJobDO job = requireJob(jobId);
        job.setStatus(status);
        jobMapper.updateById(job);
        if (STATUS_NORMAL.equals(status)) {
            jobScheduler.addOrUpdateJob(job);
        } else {
            jobScheduler.pauseJob(jobId);
        }
    }

    @Override
    public void runJob(Long jobId) {
        SystemJobDO job = requireJob(jobId);
        // 手动触发是异步提交的：runOnce 把 runner 丢进线程池后立即返回，跳过只会记在
        // sys_job_log 里，操作人永远看到成功。因此在提交前预判一次，把“任务禁止并发且正在执行”
        // 即时反馈给调用方。预判与执行之间存在窗口，并发正确性仍由 JobRunner 的 CAS 守住
        if (SystemJobDO.CONCURRENT_FORBID.equals(job.getConcurrent())
                && jobScheduler.isRunning(job.getJobId())) {
            throw new ServiceException(SystemErrorCodeConstants.JOB_RUNNING_FORBID_CONCURRENT);
        }
        jobScheduler.runOnce(job);
    }

    @Override
    public SystemJobDO getJob(Long jobId) {
        return jobMapper.selectById(jobId);
    }

    @Override
    public PageResult<SystemJobDO> getJobPage(JobPageReqVO reqVO) {
        LambdaQueryWrapper<SystemJobDO> wrapper = Wrappers.<SystemJobDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getJobName()), SystemJobDO::getJobName, reqVO.getJobName())
                .eq(StringUtils.hasText(reqVO.getJobGroup()), SystemJobDO::getJobGroup, reqVO.getJobGroup())
                .like(StringUtils.hasText(reqVO.getInvokeTarget()), SystemJobDO::getInvokeTarget, reqVO.getInvokeTarget())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemJobDO::getStatus, reqVO.getStatus())
                .orderByDesc(SystemJobDO::getJobId);
        Page<SystemJobDO> page = jobMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<SystemJobDO> getJobList() {
        return jobMapper.selectList(Wrappers.emptyWrapper());
    }

    @Override
    public LocalDateTime getNextFireTime(String cronExpression) {
        if (!StringUtils.hasText(cronExpression)) {
            return null;
        }
        try {
            return CronExpression.parse(cronExpression).next(LocalDateTime.now(clock));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void syncAllOnStartup() {
        List<SystemJobDO> jobs = jobMapper.selectList(
                Wrappers.<SystemJobDO>lambdaQuery().eq(SystemJobDO::getStatus, STATUS_NORMAL));
        for (SystemJobDO job : jobs) {
            try {
                jobScheduler.addOrUpdateJob(job);
            } catch (RuntimeException e) {
                // 启动期单任务失败不阻断整体；运维通过日志排查
                log.warn("[syncAllOnStartup] 任务注册失败 jobId={} cron={}",
                        job.getJobId(), job.getCronExpression(), e);
            }
        }
        log.info("[syncAllOnStartup] 共加载 {} 个任务", jobs.size());
    }

    // ---------- 私有校验 ----------

    private SystemJobDO requireJob(Long jobId) {
        SystemJobDO job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new ServiceException(SystemErrorCodeConstants.JOB_NOT_EXISTS);
        }
        return job;
    }

    private static void applyDefaults(SystemJobDO job) {
        if (!StringUtils.hasText(job.getJobGroup())) {
            job.setJobGroup(SystemJobDO.DEFAULT_GROUP);
        }
        if (!StringUtils.hasText(job.getStatus())) {
            job.setStatus(STATUS_NORMAL);
        }
        if (!StringUtils.hasText(job.getConcurrent())) {
            job.setConcurrent(SystemJobDO.CONCURRENT_FORBID);
        }
        if (!StringUtils.hasText(job.getMisfirePolicy())) {
            job.setMisfirePolicy(SystemJobDO.MISFIRE_DO_NOTHING);
        }
    }

    private static void validateCron(String cron) {
        if (!StringUtils.hasText(cron) || !CronExpression.isValidExpression(cron)) {
            throw new ServiceException(SystemErrorCodeConstants.JOB_INCORRECT_CRON_EXPRESSION);
        }
    }

    /**
     * 校验 invoke_target 指向的 Bean 方法存在且带 @JobHandler；
     * Bean 缺失或方法不匹配统一抛 JOB_INVOKE_TARGET_INVALID，避免泄露内部结构。
     */
    private void validateInvokeTarget(String invokeTarget) {
        InvokeTargetParser.Parsed parsed;
        try {
            parsed = InvokeTargetParser.parse(invokeTarget);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(SystemErrorCodeConstants.JOB_INVOKE_TARGET_INVALID);
        }
        Object bean;
        try {
            bean = applicationContext.getBean(parsed.beanName());
        } catch (Exception e) {
            throw new ServiceException(SystemErrorCodeConstants.JOB_INVOKE_TARGET_INVALID);
        }
        boolean matched = false;
        for (Method m : ReflectionUtils.getAllDeclaredMethods(bean.getClass())) {
            if (m.getName().equals(parsed.methodName())
                    && m.getParameterCount() == parsed.args().length
                    && m.isAnnotationPresent(JobHandler.class)) {
                matched = true;
                break;
            }
        }
        if (!matched) {
            throw new ServiceException(SystemErrorCodeConstants.JOB_INVOKE_TARGET_INVALID);
        }
    }

    private void validateNameGroupUnique(String jobName, String jobGroup, Long excludeId) {
        LambdaQueryWrapper<SystemJobDO> wrapper = Wrappers.<SystemJobDO>lambdaQuery()
                .eq(SystemJobDO::getJobName, jobName)
                .eq(SystemJobDO::getJobGroup, StringUtils.hasText(jobGroup) ? jobGroup : SystemJobDO.DEFAULT_GROUP)
                .ne(excludeId != null, SystemJobDO::getJobId, excludeId);
        if (jobMapper.selectCount(wrapper) > 0) {
            throw new ServiceException(SystemErrorCodeConstants.JOB_NAME_GROUP_DUPLICATE);
        }
    }
}

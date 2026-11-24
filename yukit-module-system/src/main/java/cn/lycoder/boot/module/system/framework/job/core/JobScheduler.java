package cn.lycoder.boot.module.system.framework.job.core;

import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobDO;
import cn.lycoder.boot.module.system.service.job.JobLogService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 任务调度门面：维护 jobId → ScheduledFuture 映射，负责注册/取消/立即执行
 * <p>
 * 调度核心使用 {@code ThreadPoolTaskScheduler}（由 {@link cn.lycoder.boot.module.system.framework.job.config.JobAutoConfiguration}
 * 提供），不依赖 Quartz。cron 表达式遵循 Spring 6 字段格式（秒 分 时 日 月 周 [年]）。
 * <p>
 * 并发标志位 {@code runningFlags} 跨调度周期持有，确保同一任务禁止并发时不会因
 * 任务重新注册而丢失"正在执行"的状态。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduler {

    private final ThreadPoolTaskScheduler jobTaskScheduler;
    private final ApplicationContext applicationContext;
    private final JobLogService jobLogService;

    /**
     * jobId → 调度 future
     */
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();
    /**
     * jobId → 是否正在运行（用于并发=禁止时的跳过控制）
     */
    private final ConcurrentHashMap<Long, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();

    /**
     * 注册或刷新任务调度：先取消旧触发器，再按当前 status 决定是否重新挂载
     */
    public void addOrUpdateJob(SystemJobDO job) {
        cancelFuture(job.getJobId());
        if (!SystemJobDO.STATUS_NORMAL.equals(job.getStatus())) {
            log.debug("[JobScheduler] 任务非运行状态，跳过注册 jobId={} status={}", job.getJobId(), job.getStatus());
            return;
        }
        try {
            CronTrigger trigger = new CronTrigger(job.getCronExpression());
            JobRunner runner = new JobRunner(job, applicationContext, jobLogService, runningFlags);
            ScheduledFuture<?> future = jobTaskScheduler.schedule(runner, trigger);
            if (future != null) {
                futures.put(job.getJobId(), future);
            }
            log.info("[JobScheduler] 任务已注册 jobId={} cron={}", job.getJobId(), job.getCronExpression());
        } catch (IllegalArgumentException e) {
            log.warn("[JobScheduler] cron 解析失败 jobId={} cron={}",
                    job.getJobId(), job.getCronExpression(), e);
            throw e;
        }
    }

    /**
     * 暂停任务：仅取消 future，保留 runningFlags 让在跑的任务自然结束
     */
    public void pauseJob(Long jobId) {
        cancelFuture(jobId);
    }

    /**
     * 移除任务：取消 future 并清理 runningFlags（任务已从 DB 删除）
     */
    public void removeJob(Long jobId) {
        cancelFuture(jobId);
        runningFlags.remove(jobId);
    }

    /**
     * 立即异步执行一次，不影响下次 cron 触发时间
     */
    public void runOnce(SystemJobDO job) {
        JobRunner runner = new JobRunner(job, applicationContext, jobLogService, runningFlags);
        jobTaskScheduler.execute(runner);
    }

    private void cancelFuture(Long jobId) {
        ScheduledFuture<?> old = futures.remove(jobId);
        if (old != null && !old.isCancelled()) {
            old.cancel(false);
        }
    }

    /**
     * 优雅停机：取消所有 future 并清空注册表
     */
    @PreDestroy
    public void shutdown() {
        futures.keySet().forEach(this::cancelFuture);
        runningFlags.clear();
    }
}

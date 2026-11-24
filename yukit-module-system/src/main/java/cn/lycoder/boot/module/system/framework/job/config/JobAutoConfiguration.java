package cn.lycoder.boot.module.system.framework.job.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时任务调度基础设施配置
 * <p>
 * 显式提供名为 {@code jobTaskScheduler} 的 {@link ThreadPoolTaskScheduler}，
 * 避免与 Spring Boot 自动配置的全局 TaskScheduler 冲突；上层业务（如 @Scheduled）仍可
 * 使用默认的 taskScheduler。
 */
@Configuration(proxyBeanMethods = false)
public class JobAutoConfiguration {

    /**
     * 调度线程池大小：监控类小任务足够；如需更大可以 @ConditionalOnMissingBean 在前覆盖
     */
    private static final int SCHEDULER_POOL_SIZE = 4;
    private static final String SCHEDULER_THREAD_PREFIX = "yukit-job-";
    /**
     * 停机等待秒数：给在跑任务一段收尾时间，避免强 kill 留下脏数据
     */
    private static final int AWAIT_TERMINATION_SECONDS = 30;

    @Bean(name = "jobTaskScheduler")
    @ConditionalOnMissingBean(name = "jobTaskScheduler")
    public ThreadPoolTaskScheduler jobTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(SCHEDULER_POOL_SIZE);
        scheduler.setThreadNamePrefix(SCHEDULER_THREAD_PREFIX);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        scheduler.initialize();
        return scheduler;
    }
}

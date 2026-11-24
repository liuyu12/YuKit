package cn.lycoder.boot.module.system.framework.job.core;

import cn.lycoder.boot.module.system.service.job.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * 启动加载器：所有单例初始化完毕后，从 DB 拉取 status='0' 的任务挂到调度器
 * <p>
 * 使用 {@link SmartInitializingSingleton} 而非 {@code @PostConstruct}，确保所有 Bean（包括
 * invoke_target 指向的业务 Bean）都已就绪再注册，避免 Cron 立即触发时找不到目标。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobStartupLoader implements SmartInitializingSingleton {

    private final JobService jobService;

    @Override
    public void afterSingletonsInstantiated() {
        try {
            jobService.syncAllOnStartup();
        } catch (Exception e) {
            // 启动加载失败不阻断应用，由运维通过日志/健康检查感知
            log.error("[JobStartupLoader] 任务启动加载异常，定时调度可能不完整", e);
        }
    }
}

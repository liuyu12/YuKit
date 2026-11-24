package cn.lycoder.boot.module.system.job;

import cn.lycoder.boot.module.system.framework.job.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 演示用任务 Bean：冒烟测试 invoke_target 链路是否打通
 * <p>
 * 生产请按业务拆分到具体 service 包并补 {@code @JobHandler} 注解。
 */
@Slf4j
@Component("sampleHeartbeatTask")
public class SampleHeartbeatTask {

    /**
     * 无参心跳
     */
    @JobHandler("演示任务：打印心跳日志")
    public void beat() {
        log.info("[SampleHeartbeatTask] beat at {}", LocalDateTime.now());
    }

    /**
     * 带参：单 String 形参，验证 invoke_target 字面量解析与类型适配
     */
    @JobHandler("演示任务：打印传入字符串")
    public void echo(String message) {
        log.info("[SampleHeartbeatTask] echo: {}", message);
    }
}

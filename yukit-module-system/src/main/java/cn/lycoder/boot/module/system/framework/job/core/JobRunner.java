package cn.lycoder.boot.module.system.framework.job.core;

import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobDO;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobLogDO;
import cn.lycoder.boot.module.system.framework.job.annotation.JobHandler;
import cn.lycoder.boot.module.system.service.job.JobLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 单次任务执行体：解析 invoke_target → 反射调用 @JobHandler 方法 → 写 sys_job_log
 * <p>
 * 作为 Runnable 由 {@code ThreadPoolTaskScheduler} 触发，也可由 JobScheduler.runOnce 即时调度。
 * <p>
 * 并发控制：每个 jobId 维护一个 AtomicBoolean；concurrent='1' 时若已在跑则直接跳过本次（写日志记录），
 * concurrent='0' 时无锁并发执行。
 */
@Slf4j
@RequiredArgsConstructor
public class JobRunner implements Runnable {

    /**
     * 异常栈最大保留行数，避免 sys_job_log.exception_info 字段溢出
     */
    private static final int MAX_STACK_LINES = 50;
    /**
     * 摘要日志模板
     */
    private static final String SUCCESS_MSG_PATTERN = "Total: %d ms";
    private static final String SKIPPED_MSG = "任务正在执行中，本次调度跳过";

    private final SystemJobDO job;
    private final ApplicationContext applicationContext;
    private final JobLogService jobLogService;
    /**
     * 跨调度周期共享的并发标志位（按 jobId 维度）
     */
    private final ConcurrentHashMap<Long, AtomicBoolean> runningFlags;
    /**
     * 全局时间源，由 JobScheduler 从容器透传（yukit.zone-id 配置）
     */
    private final Clock clock;

    @Override
    public void run() {
        AtomicBoolean flag = runningFlags.computeIfAbsent(job.getJobId(), k -> new AtomicBoolean(false));
        boolean forbidConcurrent = SystemJobDO.CONCURRENT_FORBID.equals(job.getConcurrent());
        if (forbidConcurrent && !flag.compareAndSet(false, true)) {
            recordSkipped();
            return;
        }
        LocalDateTime start = LocalDateTime.now(clock);
        long startNanos = System.nanoTime();
        try {
            invoke();
            writeLog(start, elapsed(startNanos), SystemJobLogDO.STATUS_SUCCESS, null);
        } catch (Throwable ex) {
            // 捕获一切异常写入日志，绝不冒泡打断调度线程
            log.warn("[JobRunner] 任务执行失败 jobId={} target={}", job.getJobId(), job.getInvokeTarget(), ex);
            writeLog(start, elapsed(startNanos), SystemJobLogDO.STATUS_FAILURE, stackTrace(ex));
        } finally {
            if (forbidConcurrent) {
                flag.set(false);
            }
        }
    }

    private void invoke() throws Exception {
        InvokeTargetParser.Parsed parsed;
        try {
            parsed = InvokeTargetParser.parse(job.getInvokeTarget());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("invoke_target 解析失败: " + e.getMessage(), e);
        }
        Object bean;
        try {
            bean = applicationContext.getBean(parsed.beanName());
        } catch (Exception e) {
            throw new IllegalStateException("找不到 Spring Bean: " + parsed.beanName(), e);
        }
        Method method = findHandlerMethod(bean.getClass(), parsed.methodName(), parsed.args().length);
        if (method == null) {
            throw new IllegalStateException("Bean 方法不存在或未标注 @JobHandler: "
                    + parsed.beanName() + "." + parsed.methodName());
        }
        Object[] adapted = adaptArgs(method, parsed.args());
        // ReflectionUtils.invokeMethod 内部已拆解 InvocationTargetException，
        // 这里不再包装，原始异常会冒泡到 run() 的 catch。
        ReflectionUtils.invokeMethod(method, bean, adapted);
    }

    private static Method findHandlerMethod(Class<?> clazz, String name, int argCount) {
        for (Method m : ReflectionUtils.getAllDeclaredMethods(clazz)) {
            if (!m.getName().equals(name) || m.getParameterCount() != argCount) {
                continue;
            }
            if (m.isAnnotationPresent(JobHandler.class)) {
                ReflectionUtils.makeAccessible(m);
                return m;
            }
        }
        return null;
    }

    /**
     * 把解析出的字面量适配到方法形参类型；不支持的转换交回原值，由反射层抛 ClassCastException。
     */
    private static Object[] adaptArgs(Method method, Object[] raw) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] out = new Object[raw.length];
        for (int i = 0; i < raw.length; i++) {
            out[i] = coerce(raw[i], paramTypes[i]);
        }
        return out;
    }

    private static Object coerce(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        if (value instanceof Number n) {
            if (targetType == int.class || targetType == Integer.class) {
                return n.intValue();
            }
            if (targetType == long.class || targetType == Long.class) {
                return n.longValue();
            }
            if (targetType == double.class || targetType == Double.class) {
                return n.doubleValue();
            }
            if (targetType == float.class || targetType == Float.class) {
                return n.floatValue();
            }
        }
        if (value instanceof Boolean b) {
            if (targetType == boolean.class || targetType == Boolean.class) {
                return b;
            }
        }
        if (targetType == String.class) {
            return String.valueOf(value);
        }
        return value;
    }

    private static long elapsed(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
    }

    private void writeLog(LocalDateTime start, long durationMs, String status, String exceptionInfo) {
        SystemJobLogDO jobLog = new SystemJobLogDO();
        jobLog.setJobId(job.getJobId());
        jobLog.setJobName(job.getJobName());
        jobLog.setJobGroup(job.getJobGroup());
        jobLog.setInvokeTarget(job.getInvokeTarget());
        jobLog.setJobMessage(String.format(SUCCESS_MSG_PATTERN, durationMs));
        jobLog.setStatus(status);
        jobLog.setExceptionInfo(exceptionInfo);
        jobLog.setStartTime(start);
        jobLog.setEndTime(start.plusNanos(durationMs * 1_000_000L));
        jobLog.setDurationMs(durationMs);
        try {
            jobLogService.createJobLog(jobLog);
        } catch (Exception e) {
            // 日志写失败不能影响主任务，只打 warn
            log.warn("[JobRunner] 写调度日志失败 jobId={}", job.getJobId(), e);
        }
    }

    private void recordSkipped() {
        LocalDateTime now = LocalDateTime.now(clock);
        SystemJobLogDO jobLog = new SystemJobLogDO();
        jobLog.setJobId(job.getJobId());
        jobLog.setJobName(job.getJobName());
        jobLog.setJobGroup(job.getJobGroup());
        jobLog.setInvokeTarget(job.getInvokeTarget());
        jobLog.setJobMessage(SKIPPED_MSG);
        jobLog.setStatus(SystemJobLogDO.STATUS_SUCCESS);
        jobLog.setExceptionInfo("");
        jobLog.setStartTime(now);
        jobLog.setEndTime(now);
        jobLog.setDurationMs(0L);
        try {
            jobLogService.createJobLog(jobLog);
        } catch (Exception e) {
            log.warn("[JobRunner] 写跳过日志失败 jobId={}", job.getJobId(), e);
        }
    }

    private static String stackTrace(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Throwable cur = ex;
        int lines = 0;
        while (cur != null && lines < MAX_STACK_LINES) {
            sb.append(cur.getClass().getName()).append(": ").append(cur.getMessage()).append('\n');
            for (StackTraceElement el : cur.getStackTrace()) {
                if (lines++ >= MAX_STACK_LINES) {
                    break;
                }
                sb.append("\tat ").append(el).append('\n');
            }
            cur = cur.getCause() == cur ? null : cur.getCause();
        }
        return sb.toString();
    }
}

package cn.lycoder.boot.module.system.dal.dataobject.job;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务执行日志 DO，对应表 sys_job_log
 * <p>
 * 作为每次任务调度的不可变快照，与 sys_operate_log/sys_login_log 同策略不继承 BaseEntity；
 * 任务删除时仅将 {@code jobId} 置 NULL，保留历史日志用于审计。
 */
@Data
@TableName("sys_job_log")
public class SystemJobLogDO implements Serializable {

    /**
     * 执行状态 - 成功
     */
    public static final String STATUS_SUCCESS = "0";
    /**
     * 执行状态 - 失败
     */
    public static final String STATUS_FAILURE = "1";

    /**
     * 日志主键
     */
    @TableId
    private Long jobLogId;

    /**
     * 关联任务 ID（任务删除后置 NULL，保留历史）
     */
    private Long jobId;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务组名
     */
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    private String invokeTarget;

    /**
     * 日志摘要（耗时/结果）
     */
    private String jobMessage;

    /**
     * 执行状态：0 成功 / 1 失败
     */
    private String status;

    /**
     * 异常栈信息（截断存储）
     */
    private String exceptionInfo;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 记录时间
     */
    private LocalDateTime createTime;
}

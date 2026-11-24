package cn.lycoder.boot.module.system.dal.dataobject.job;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务 DO，对应表 sys_job
 * <p>
 * 字段语义与 RuoYi 一致；misfirePolicy/concurrent/status 使用字典 {@code sys_job_*} 编码。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job")
public class SystemJobDO extends BaseEntity {

    /**
     * 任务状态 - 正常
     */
    public static final String STATUS_NORMAL = "0";
    /**
     * 任务状态 - 暂停
     */
    public static final String STATUS_PAUSE = "1";

    /**
     * 并发策略 - 允许
     */
    public static final String CONCURRENT_ALLOW = "0";
    /**
     * 并发策略 - 禁止
     */
    public static final String CONCURRENT_FORBID = "1";

    /**
     * Misfire 策略 - 立即执行
     */
    public static final String MISFIRE_FIRE_NOW = "1";
    /**
     * Misfire 策略 - 执行一次
     */
    public static final String MISFIRE_FIRE_ONCE = "2";
    /**
     * Misfire 策略 - 放弃执行
     */
    public static final String MISFIRE_DO_NOTHING = "3";

    /**
     * 默认任务组
     */
    public static final String DEFAULT_GROUP = "DEFAULT";

    /**
     * 任务ID
     */
    @TableId
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
     * 调用目标字符串，形如 {@code beanName.methodName(arg1,arg2)}
     */
    private String invokeTarget;

    /**
     * cron 表达式（Spring 格式：秒 分 时 日 月 周 [年]）
     */
    private String cronExpression;

    /**
     * 计划执行错误策略：1 立即执行 / 2 执行一次 / 3 放弃执行
     */
    private String misfirePolicy;

    /**
     * 是否允许并发执行：0 允许 / 1 禁止
     */
    private String concurrent;

    /**
     * 状态：0 正常 / 1 暂停
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}

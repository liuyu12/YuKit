package cn.lycoder.boot.module.system.dal.dataobject.logger;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志 DO，对应表 sys_login_log
 * <p>
 * 与 {@code SystemOperateLogDO} 同为不可变运行期快照，记录本身即含结果与时间，
 * 不参与逻辑删除/审计自动填充，故不继承 BaseEntity。
 */
@Data
@TableName("sys_login_log")
public class SystemLoginLogDO implements Serializable {

    /**
     * 日志主键
     */
    @TableId
    private Long id;

    /**
     * 登录用户 ID（账号不存在时为 null）
     */
    private Long userId;

    /**
     * 登录账号名
     */
    private String username;

    /**
     * 登录结果，见 LoginResultTypeEnum
     */
    private Integer result;

    /**
     * 结果描述（失败原因 / 剩余尝试次数等）
     */
    private String msg;

    /**
     * 客户端 IP
     */
    private String userIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 记录时间
     */
    private LocalDateTime createTime;
}

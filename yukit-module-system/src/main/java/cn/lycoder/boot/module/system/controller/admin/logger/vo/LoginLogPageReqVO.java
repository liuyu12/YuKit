package cn.lycoder.boot.module.system.controller.admin.logger.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogPageReqVO extends PageParam {

    /**
     * 登录账号，模糊匹配
     */
    @Schema(description = "登录账号，模糊匹配", example = "admin")
    private String username;

    /**
     * 登录结果，精确匹配，见 LoginResultTypeEnum
     */
    @Schema(description = "登录结果：0成功 1密码错误 2禁用 3锁定 4验证码 5系统异常", example = "0")
    private Integer result;

    /**
     * 客户端 IP，模糊匹配
     */
    @Schema(description = "客户端IP，模糊匹配", example = "127.0.0.1")
    private String userIp;

    /**
     * 登录时间范围 - 起
     */
    @Schema(description = "登录时间范围-起")
    private LocalDateTime beginTime;

    /**
     * 登录时间范围 - 止
     */
    @Schema(description = "登录时间范围-止")
    private LocalDateTime endTime;
}

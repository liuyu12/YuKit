package cn.lycoder.boot.module.system.controller.admin.logger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志 Response VO
 */
@Data
public class LoginLogRespVO implements Serializable {

    @Schema(description = "日志主键")
    private Long id;

    @Schema(description = "登录用户ID")
    private Long userId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "登录结果码")
    private Integer result;

    @Schema(description = "登录结果描述")
    private String resultDescription;

    @Schema(description = "结果补充说明")
    private String msg;

    @Schema(description = "客户端IP")
    private String userIp;

    @Schema(description = "浏览器UA")
    private String userAgent;

    @Schema(description = "记录时间")
    private LocalDateTime createTime;
}

package cn.lycoder.boot.module.system.controller.admin.online.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线会话 Response VO
 */
@Data
public class OnlineSessionRespVO implements Serializable {

    @Schema(description = "会话令牌（强制下线以此为准）")
    private String tokenValue;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "浏览器UA")
    private String userAgent;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}

package cn.lycoder.boot.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRespVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 令牌名称（前端据此设置请求头，对应 sa-token.token-name）
     */
    @Schema(description = "令牌名称（前端据此设置请求头，对应 sa-token.token-name）", example = "satoken")
    private String tokenName;

    /**
     * 令牌值
     */
    @Schema(description = "令牌值", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String tokenValue;
}

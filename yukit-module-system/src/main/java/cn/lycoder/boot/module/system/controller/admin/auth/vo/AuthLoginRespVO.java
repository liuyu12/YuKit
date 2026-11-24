package cn.lycoder.boot.module.system.controller.admin.auth.vo;

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
    private Long userId;

    /**
     * 令牌名称（前端据此设置请求头，对应 sa-token.token-name）
     */
    private String tokenName;

    /**
     * 令牌值
     */
    private String tokenValue;
}

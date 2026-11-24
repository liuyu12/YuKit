package cn.lycoder.boot.module.system.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息 Response VO
 * <p>
 * 注意：绝不包含 password、delFlag 等敏感/内部字段
 */
@Data
public class UserRespVO implements Serializable {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号", example = "zhangsan")
    private String userName;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "张三")
    private String nickName;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", example = "13800138000")
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @Schema(description = "用户性别（0男 1女 2未知）", example = "0")
    private String sex;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址", example = "https://example.com/avatar.png")
    private String avatar;

    /**
     * 账号状态（0正常 1停用）
     */
    @Schema(description = "账号状态（0正常 1停用）", example = "0")
    private String status;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP", example = "127.0.0.1")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间", example = "2026-01-01 12:00:00")
    private LocalDateTime loginDate;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "初始管理员账号")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private LocalDateTime createTime;
}

package cn.lycoder.boot.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人中心 - 我的资料 Response VO
 * <p>
 * 相比管理侧 UserRespVO 剔除 deptId/remark 等他人不应感知的字段
 */
@Data
@Schema(description = "个人中心 - 我的资料 Response VO")
public class UserProfileRespVO {

    @Schema(description = "用户ID", example = "2")
    private Long userId;

    @Schema(description = "用户账号", example = "ry")
    private String userName;

    @Schema(description = "用户昵称", example = "若依")
    private String nickName;

    @Schema(description = "邮箱", example = "ry@163.com")
    private String email;

    @Schema(description = "手机号码", example = "15888888888")
    private String phonenumber;

    @Schema(description = "用户性别（0男 1女 2未知）", example = "1")
    private String sex;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "账号创建时间")
    private LocalDateTime createTime;
}

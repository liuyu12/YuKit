package cn.lycoder.boot.module.system.controller.admin.auth.vo;

import cn.lycoder.boot.framework.common.validation.InDict;
import cn.lycoder.boot.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人中心 - 修改我的资料 Request VO
 * <p>
 * 仅开放昵称/邮箱/手机号/性别四个自助字段；账号、部门、状态、角色等归管理员侧用户管理
 */
@Data
@Schema(description = "个人中心 - 修改我的资料 Request VO")
public class UserProfileUpdateReqVO {

    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "若依")
    private String nickName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "用户邮箱长度不能超过50个字符")
    @Schema(description = "邮箱", example = "ry@163.com")
    private String email;

    @Mobile(message = "手机号码格式不正确")
    @Schema(description = "手机号码", example = "15888888888")
    private String phonenumber;

    @InDict("sys_user_sex")
    @Schema(description = "用户性别（0男 1女 2未知），取值联动字典 sys_user_sex", example = "1")
    private String sex;
}

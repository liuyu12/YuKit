package cn.lycoder.boot.module.system.controller.admin.user.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建 Request VO
 */
@Data
public class UserCreateReqVO {

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    @Size(max = 30, message = "用户账号长度不能超过30个字符")
    @Pattern(regexp = "^[a-zA-Z]\\w{2,29}$", message = "用户账号必须以字母开头，仅支持字母、数字、下划线，长度3-30")
    private String userName;

    /**
     * 用户密码（明文，服务端 BCrypt 加密后存储）
     */
    @NotBlank(message = "用户密码不能为空")
    @Size(min = 6, max = 30, message = "用户密码长度需在6-30个字符之间")
    private String password;

    /**
     * 用户昵称
     */
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickName;

    /**
     * 用户邮箱
     */
    @Email(message = "用户邮箱格式不正确")
    @Size(max = 50, message = "用户邮箱长度不能超过50个字符")
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 账号状态（0正常 1停用），不传默认正常
     */
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}

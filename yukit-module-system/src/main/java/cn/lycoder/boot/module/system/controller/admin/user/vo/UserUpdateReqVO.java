package cn.lycoder.boot.module.system.controller.admin.user.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户更新 Request VO
 * <p>
 * 不继承 CreateReqVO：更新不允许改账号名（唯一标识）、不在此改密码（独立重置流程）
 */
@Data
public class UserUpdateReqVO {

    /**
     * 用户ID（REST 风格下由路径参数传入并覆盖，请求体可省略）
     */
    private Long userId;

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
     * 账号状态（0正常 1停用）
     */
    @NotBlank(message = "账号状态不能为空")
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}

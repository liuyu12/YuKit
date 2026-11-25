package cn.lycoder.boot.module.system.controller.admin.user.vo;

import cn.lycoder.boot.framework.common.validation.InDict;
import cn.lycoder.boot.framework.common.validation.InEnum;
import cn.lycoder.boot.framework.common.validation.Mobile;
import cn.lycoder.boot.module.system.enums.common.CommonStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

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
    @Schema(description = "用户账号（登录名，唯一）", example = "zhangsan")
    private String userName;

    /**
     * 用户密码（明文，服务端 BCrypt 加密后存储）
     */
    @NotBlank(message = "用户密码不能为空")
    @Size(min = 6, max = 30, message = "用户密码长度需在6-30个字符之间")
    @Schema(description = "用户密码（明文，服务端 BCrypt 加密后存储）", example = "admin123")
    private String password;

    /**
     * 用户昵称
     */
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    @Schema(description = "用户昵称", example = "张三")
    private String nickName;

    /**
     * 用户邮箱
     */
    @Email(message = "用户邮箱格式不正确")
    @Size(max = 50, message = "用户邮箱长度不能超过50个字符")
    @Schema(description = "用户邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号码
     */
    @Mobile(message = "手机号码格式不正确")
    @Schema(description = "手机号码", example = "13800138000")
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知），取值联动字典 sys_user_sex
     */
    @InDict("sys_user_sex")
    @Schema(description = "用户性别（0男 1女 2未知），取值联动字典 sys_user_sex", example = "0")
    private String sex;

    /**
     * 账号状态（0正常 1停用），不传默认正常
     */
    @InEnum(CommonStatusEnum.class)
    @Schema(description = "账号状态（0正常 1停用），不传默认正常", example = "0")
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "初始管理员账号")
    private String remark;

    /**
     * 关联岗位ID集合（表单多选，null/空表示不分配任何岗位）
     */
    @Schema(description = "关联岗位ID集合（多选）", example = "[1,2]")
    private List<Long> postIds;
}

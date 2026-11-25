package cn.lycoder.boot.module.system.controller.admin.user.vo;

import cn.lycoder.boot.framework.common.validation.InDict;
import cn.lycoder.boot.framework.common.validation.InEnum;
import cn.lycoder.boot.framework.common.validation.Mobile;
import cn.lycoder.boot.module.system.enums.common.CommonStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

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
    @Schema(description = "用户ID（由路径参数传入并覆盖，请求体可省略）", example = "1")
    private Long userId;

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
     * 账号状态（0正常 1停用）
     */
    @NotBlank(message = "账号状态不能为空")
    @InEnum(CommonStatusEnum.class)
    @Schema(description = "账号状态（0正常 1停用）", example = "0")
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "日常维护中")
    private String remark;

    /**
     * 关联岗位ID集合（表单多选，全量覆盖：null/空表示清空该用户全部岗位）
     */
    @Schema(description = "关联岗位ID集合（多选，全量覆盖）", example = "[1,2]")
    private List<Long> postIds;
}

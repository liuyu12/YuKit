package cn.lycoder.boot.module.system.controller.admin.dept.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门创建 Request VO
 */
@Data
public class DeptCreateReqVO {

    /**
     * 父部门ID，根节点为 0
     */
    @NotNull(message = "父部门不能为空")
    @Schema(description = "父部门ID，根节点为 0", example = "0")
    private Long parentId;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 30, message = "部门名称长度不能超过 30 个字符")
    @Schema(description = "部门名称", example = "研发部门")
    private String deptName;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    @Schema(description = "显示顺序", example = "1")
    private Integer orderNum;

    /**
     * 负责人
     */
    @Schema(description = "负责人", example = "若依")
    private String leader;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "15888888888")
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "ry@163.com")
    private String email;

    /**
     * 部门状态（0正常 1停用），默认正常
     */
    @Schema(description = "部门状态（0正常 1停用）", example = "0")
    private String status;
}

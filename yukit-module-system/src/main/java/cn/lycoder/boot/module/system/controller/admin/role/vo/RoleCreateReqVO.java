package cn.lycoder.boot.module.system.controller.admin.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色创建 Request VO
 */
@Data
public class RoleCreateReqVO {

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "运营角色")
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色权限字符串
     */
    @Schema(description = "角色权限字符串", example = "operator")
    @NotBlank(message = "角色权限字符不能为空")
    private String roleKey;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序", example = "1")
    @NotNull(message = "显示顺序不能为空")
    private Integer roleSort;

    /**
     * 角色状态（0正常 1停用）
     */
    @Schema(description = "角色状态（0正常 1停用）", example = "0")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "负责日常运营")
    private String remark;

    /**
     * 关联的菜单ID列表（创建时一并授权）
     */
    @Schema(description = "关联的菜单ID列表（创建时一并授权）")
    private List<Long> menuIds;
}

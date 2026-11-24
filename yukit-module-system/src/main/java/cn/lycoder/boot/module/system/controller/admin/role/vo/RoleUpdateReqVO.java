package cn.lycoder.boot.module.system.controller.admin.role.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色更新 Request VO
 * <p>
 * 独立于创建 VO：roleId 由路径参数传入，menuIds 授权走独立的分配接口
 */
@Data
public class RoleUpdateReqVO {

    /**
     * 角色ID（由路径参数传入并覆盖，请求体可省略）
     */
    private Long roleId;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色权限字符串
     */
    @NotBlank(message = "角色权限字符不能为空")
    private String roleKey;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    private Integer roleSort;

    /**
     * 角色状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}

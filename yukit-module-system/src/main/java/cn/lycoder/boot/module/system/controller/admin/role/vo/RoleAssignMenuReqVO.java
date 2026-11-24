package cn.lycoder.boot.module.system.controller.admin.role.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色分配菜单（授权）Request VO
 * <p>
 * roleId 由路径参数传入
 */
@Data
public class RoleAssignMenuReqVO {

    /**
     * 授权的菜单ID列表，全量覆盖
     */
    @NotNull(message = "菜单列表不能为空")
    private List<Long> menuIds;
}

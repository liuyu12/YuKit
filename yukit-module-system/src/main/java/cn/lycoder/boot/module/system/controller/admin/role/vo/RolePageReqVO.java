package cn.lycoder.boot.module.system.controller.admin.role.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageReqVO extends PageParam {

    /**
     * 角色名称，模糊匹配
     */
    @Schema(description = "角色名称，模糊匹配", example = "运营")
    private String roleName;

    /**
     * 角色权限字符串，模糊匹配
     */
    @Schema(description = "角色权限字符串，模糊匹配", example = "operator")
    private String roleKey;

    /**
     * 角色状态（0正常 1停用）
     */
    @Schema(description = "角色状态（0正常 1停用）", example = "0")
    private String status;
}

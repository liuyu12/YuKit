package cn.lycoder.boot.module.system.controller.admin.role.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
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
    private String roleName;

    /**
     * 角色权限字符串，模糊匹配
     */
    private String roleKey;

    /**
     * 角色状态（0正常 1停用）
     */
    private String status;
}

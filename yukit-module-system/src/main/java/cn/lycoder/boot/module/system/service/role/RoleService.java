package cn.lycoder.boot.module.system.service.role;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RolePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.role.SystemRoleDO;

import java.util.List;

/**
 * 角色管理 Service 接口
 */
public interface RoleService {

    /**
     * 创建角色，menuIds 非空时一并完成菜单授权
     */
    Long createRole(RoleCreateReqVO reqVO);

    /**
     * 更新角色基础信息
     */
    void updateRole(RoleUpdateReqVO reqVO);

    /**
     * 删除角色（逻辑删除），并清理菜单授权关系
     */
    void deleteRole(Long roleId);

    /**
     * 获取角色详情
     */
    SystemRoleDO getRole(Long roleId);

    /**
     * 分页查询角色
     */
    PageResult<SystemRoleDO> getRolePage(RolePageReqVO reqVO);

    /**
     * 获取全部正常状态角色（下拉列表）
     */
    List<SystemRoleDO> getRoleList();

    /**
     * 角色授权菜单（全量覆盖）
     */
    void assignRoleMenu(Long roleId, List<Long> menuIds);

    /**
     * 获取角色已授权的菜单ID集合
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
}

package cn.lycoder.boot.module.system.controller.admin.role;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleAssignMenuReqVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RolePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleRespVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleUpdateReqVO;
import cn.lycoder.boot.module.system.convert.role.RoleConvert;
import cn.lycoder.boot.module.system.dal.dataobject.role.SystemRoleDO;
import cn.lycoder.boot.module.system.service.role.RoleService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理
 * <p>
 * RESTful 风格 + Sa-Token 细粒度权限，权限码与 sys_menu.perms 对应
 */
@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RoleConvert roleConvert;

    /**
     * 创建角色（可携带 menuIds 一并授权）
     */
    @PostMapping
    @SaCheckPermission("system:role:create")
    public Result<Long> createRole(@RequestBody @Valid RoleCreateReqVO reqVO) {
        return Result.success(roleService.createRole(reqVO));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{roleId}")
    @SaCheckPermission("system:role:update")
    public Result<Boolean> updateRole(@PathVariable("roleId") Long roleId,
                                      @RequestBody @Valid RoleUpdateReqVO reqVO) {
        reqVO.setRoleId(roleId);
        roleService.updateRole(reqVO);
        return Result.success(true);
    }

    /**
     * 删除角色（逻辑删除，同时清理菜单授权）
     */
    @DeleteMapping("/{roleId}")
    @SaCheckPermission("system:role:delete")
    public Result<Boolean> deleteRole(@PathVariable("roleId") Long roleId) {
        roleService.deleteRole(roleId);
        return Result.success(true);
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{roleId}")
    @SaCheckPermission("system:role:query")
    public Result<RoleRespVO> getRole(@PathVariable("roleId") Long roleId) {
        SystemRoleDO role = roleService.getRole(roleId);
        return Result.success(role == null ? null : roleConvert.convert(role));
    }

    /**
     * 分页查询角色
     */
    @GetMapping
    @SaCheckPermission("system:role:query")
    public Result<PageResult<RoleRespVO>> getRolePage(@Valid RolePageReqVO reqVO) {
        PageResult<SystemRoleDO> pageResult = roleService.getRolePage(reqVO);
        return Result.success(roleConvert.convertPage(pageResult));
    }

    /**
     * 正常状态角色简单列表（用户分配角色等下拉场景）
     */
    @GetMapping("/simple-list")
    @SaCheckPermission("system:role:query")
    public Result<List<RoleRespVO>> getRoleSimpleList() {
        return Result.success(roleConvert.convertList(roleService.getRoleList()));
    }

    /**
     * 角色授权菜单（全量覆盖）
     */
    @PutMapping("/{roleId}/menus")
    @SaCheckPermission("system:role:assign")
    public Result<Boolean> assignRoleMenu(@PathVariable("roleId") Long roleId,
                                          @RequestBody @Valid RoleAssignMenuReqVO reqVO) {
        roleService.assignRoleMenu(roleId, reqVO.getMenuIds());
        return Result.success(true);
    }

    /**
     * 查询角色已授权的菜单ID集合（授权回显）
     */
    @GetMapping("/{roleId}/menus")
    @SaCheckPermission("system:role:query")
    public Result<List<Long>> getMenuIdsByRoleId(@PathVariable("roleId") Long roleId) {
        return Result.success(roleService.getMenuIdsByRoleId(roleId));
    }
}

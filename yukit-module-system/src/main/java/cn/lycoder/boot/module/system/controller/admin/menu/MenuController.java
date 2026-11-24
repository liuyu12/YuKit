package cn.lycoder.boot.module.system.controller.admin.menu;

import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuListReqVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuRespVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuUpdateReqVO;
import cn.lycoder.boot.module.system.convert.menu.MenuConvert;
import cn.lycoder.boot.module.system.dal.dataobject.menu.SystemMenuDO;
import cn.lycoder.boot.module.system.service.menu.MenuService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 菜单管理
 * <p>
 * RESTful 风格 + Sa-Token 细粒度权限，权限码与 sys_menu.perms 对应
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final MenuConvert menuConvert;

    /**
     * 创建菜单
     */
    @Operation(summary = "创建菜单")
    @PostMapping
    @SaCheckPermission("system:menu:create")
    public Result<Long> createMenu(@RequestBody @Valid MenuCreateReqVO reqVO) {
        return Result.success(menuService.createMenu(reqVO));
    }

    /**
     * 更新菜单
     */
    @Operation(summary = "更新菜单")
    @PutMapping("/{menuId}")
    @SaCheckPermission("system:menu:update")
    public Result<Boolean> updateMenu(@Parameter(description = "菜单ID") @PathVariable("menuId") Long menuId,
                                      @RequestBody @Valid MenuUpdateReqVO reqVO) {
        reqVO.setMenuId(menuId);
        menuService.updateMenu(reqVO);
        return Result.success(true);
    }

    /**
     * 删除菜单（存在子菜单时拒绝）
     */
    @Operation(summary = "删除菜单（存在子菜单时拒绝）")
    @DeleteMapping("/{menuId}")
    @SaCheckPermission("system:menu:delete")
    public Result<Boolean> deleteMenu(@Parameter(description = "菜单ID") @PathVariable("menuId") Long menuId) {
        menuService.deleteMenu(menuId);
        return Result.success(true);
    }

    /**
     * 获取菜单详情
     */
    @Operation(summary = "获取菜单详情")
    @GetMapping("/{menuId}")
    @SaCheckPermission("system:menu:query")
    public Result<MenuRespVO> getMenu(@Parameter(description = "菜单ID") @PathVariable("menuId") Long menuId) {
        SystemMenuDO menu = menuService.getMenu(menuId);
        return Result.success(menu == null ? null : menuConvert.convert(menu));
    }

    /**
     * 菜单树（管理列表）
     */
    @Operation(summary = "菜单树（管理列表）")
    @GetMapping
    @SaCheckPermission("system:menu:query")
    public Result<List<MenuRespVO>> getMenuTree(MenuListReqVO reqVO) {
        return Result.success(menuService.getMenuTree(reqVO));
    }

    /**
     * 菜单树选择（角色授权时的候选树，仅正常状态）
     */
    @Operation(summary = "菜单树选择（角色授权候选树，仅正常状态）")
    @GetMapping("/tree-select")
    @SaCheckPermission("system:menu:query")
    public Result<List<MenuRespVO>> getMenuTreeForSelect() {
        return Result.success(menuService.getMenuTreeForSelect());
    }
}

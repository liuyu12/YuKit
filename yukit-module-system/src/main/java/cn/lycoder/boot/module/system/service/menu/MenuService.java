package cn.lycoder.boot.module.system.service.menu;

import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuListReqVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuRespVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.menu.SystemMenuDO;

import java.util.List;

/**
 * 菜单管理 Service 接口
 */
public interface MenuService {

    /**
     * 创建菜单
     */
    Long createMenu(MenuCreateReqVO reqVO);

    /**
     * 更新菜单
     */
    void updateMenu(MenuUpdateReqVO reqVO);

    /**
     * 删除菜单（物理删除，存在子菜单时拒绝）
     */
    void deleteMenu(Long menuId);

    /**
     * 获取菜单详情
     */
    SystemMenuDO getMenu(Long menuId);

    /**
     * 获取菜单树（管理列表）
     */
    List<MenuRespVO> getMenuTree(MenuListReqVO reqVO);

    /**
     * 获取全部启用菜单树（角色授权时的候选树）
     */
    List<MenuRespVO> getMenuTreeForSelect();
}

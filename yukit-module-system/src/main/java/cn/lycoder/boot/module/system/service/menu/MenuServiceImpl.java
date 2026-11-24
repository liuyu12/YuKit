package cn.lycoder.boot.module.system.service.menu;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuListReqVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuRespVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuUpdateReqVO;
import cn.lycoder.boot.module.system.convert.menu.MenuConvert;
import cn.lycoder.boot.module.system.dal.dataobject.menu.SystemMenuDO;
import cn.lycoder.boot.module.system.dal.mapper.menu.SystemMenuMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.*;

/**
 * 菜单管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SystemMenuMapper menuMapper;
    private final MenuConvert menuConvert;

    @Override
    public Long createMenu(MenuCreateReqVO reqVO) {
        // 1. 校验父菜单、名称与权限标识
        validateParentMenu(reqVO.getParentId(), null);
        validateMenuNameUnique(reqVO.getParentId(), reqVO.getMenuName(), null);
        validatePermsUnique(reqVO.getPerms(), null);
        // 2. ReqVO → DO，审计字段由 MetaObjectHandler 填充
        SystemMenuDO menu = menuConvert.convert(reqVO);
        menu.setVisible(StringUtils.hasText(reqVO.getVisible())
                ? reqVO.getVisible() : "0");
        menu.setStatus(StringUtils.hasText(reqVO.getStatus())
                ? reqVO.getStatus() : "0");
        menuMapper.insert(menu);
        return menu.getMenuId();
    }

    @Override
    public void updateMenu(MenuUpdateReqVO reqVO) {
        // 1. 校验存在、父菜单（不能是自己）、名称与权限标识
        validateMenuExists(reqVO.getMenuId());
        validateParentMenu(reqVO.getParentId(), reqVO.getMenuId());
        validateMenuNameUnique(reqVO.getParentId(), reqVO.getMenuName(), reqVO.getMenuId());
        validatePermsUnique(reqVO.getPerms(), reqVO.getMenuId());
        // 2. 更新
        SystemMenuDO updateObj = menuConvert.convert(reqVO);
        menuMapper.updateById(updateObj);
    }

    @Override
    public void deleteMenu(Long menuId) {
        validateMenuExists(menuId);
        // 存在子菜单时拒绝删除，避免产生孤儿节点
        Long children = menuMapper.selectCount(Wrappers.<SystemMenuDO>lambdaQuery()
                .eq(SystemMenuDO::getParentId, menuId));
        if (children != null && children > 0) {
            throw new ServiceException(MENU_EXITS_CHILDREN);
        }
        // sys_menu 无 del_flag，物理删除
        menuMapper.deleteById(menuId);
    }

    @Override
    public SystemMenuDO getMenu(Long menuId) {
        return menuMapper.selectById(menuId);
    }

    @Override
    public List<MenuRespVO> getMenuTree(MenuListReqVO reqVO) {
        List<SystemMenuDO> menus = menuMapper.selectList(Wrappers.<SystemMenuDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getMenuName()), SystemMenuDO::getMenuName, reqVO.getMenuName())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemMenuDO::getStatus, reqVO.getStatus())
                .eq(StringUtils.hasText(reqVO.getMenuType()), SystemMenuDO::getMenuType, reqVO.getMenuType())
                .orderByAsc(SystemMenuDO::getParentId)
                .orderByAsc(SystemMenuDO::getOrderNum));
        return buildTree(menus);
    }

    @Override
    public List<MenuRespVO> getMenuTreeForSelect() {
        List<SystemMenuDO> menus = menuMapper.selectList(Wrappers.<SystemMenuDO>lambdaQuery()
                .eq(SystemMenuDO::getStatus, "0")
                .orderByAsc(SystemMenuDO::getParentId)
                .orderByAsc(SystemMenuDO::getOrderNum));
        return buildTree(menus);
    }

    /**
     * 平铺列表 → 树。一次分组避免递归全表扫描；
     * 条件过滤后若无根节点命中（如按名称搜到的是子节点），退化为平铺列表返回
     */
    private List<MenuRespVO> buildTree(List<SystemMenuDO> menus) {
        List<MenuRespVO> nodes = menuConvert.convertList(menus);
        Map<Long, List<MenuRespVO>> childrenMap = nodes.stream()
                .collect(Collectors.groupingBy(MenuRespVO::getParentId));
        List<MenuRespVO> roots = new ArrayList<>();
        for (MenuRespVO node : nodes) {
            node.setChildren(childrenMap.get(node.getMenuId()));
            if (SystemMenuDO.ROOT_PARENT_ID.equals(node.getParentId())) {
                roots.add(node);
            }
        }
        return roots.isEmpty() ? nodes : roots;
    }

    private SystemMenuDO validateMenuExists(Long menuId) {
        SystemMenuDO menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new ServiceException(MENU_NOT_EXISTS);
        }
        return menu;
    }

    /**
     * 校验父菜单合法：非根时父菜单必须存在，且更新时不能选择自己作为父
     */
    private void validateParentMenu(Long parentId, Long currentMenuId) {
        if (parentId == null || SystemMenuDO.ROOT_PARENT_ID.equals(parentId)) {
            return;
        }
        if (parentId.equals(currentMenuId)) {
            throw new ServiceException(MENU_PARENT_ERROR);
        }
        if (menuMapper.selectById(parentId) == null) {
            throw new ServiceException(MENU_PARENT_NOT_EXISTS);
        }
    }

    private void validateMenuNameUnique(Long parentId, String menuName, Long excludeMenuId) {
        Long count = menuMapper.selectCount(Wrappers.<SystemMenuDO>lambdaQuery()
                .eq(SystemMenuDO::getParentId, parentId)
                .eq(SystemMenuDO::getMenuName, menuName)
                .ne(excludeMenuId != null, SystemMenuDO::getMenuId, excludeMenuId));
        if (count != null && count > 0) {
            throw new ServiceException(MENU_NAME_EXISTS);
        }
    }

    private void validatePermsUnique(String perms, Long excludeMenuId) {
        if (!StringUtils.hasText(perms)) {
            return;
        }
        Long count = menuMapper.selectCount(Wrappers.<SystemMenuDO>lambdaQuery()
                .eq(SystemMenuDO::getPerms, perms)
                .ne(excludeMenuId != null, SystemMenuDO::getMenuId, excludeMenuId));
        if (count != null && count > 0) {
            throw new ServiceException(MENU_PERMS_EXISTS);
        }
    }
}

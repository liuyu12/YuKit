package cn.lycoder.boot.module.system.framework.security;

import cn.lycoder.boot.module.system.dal.mapper.menu.SystemMenuMapper;
import cn.lycoder.boot.module.system.dal.mapper.role.SystemRoleMapper;
import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据源：告诉框架"某个账号拥有哪些权限码/角色码"
 * <p>
 * 校验链路：@SaCheckPermission("system:role:create") → Sa-Token 回调本类
 * → 用户 → sys_user_role → sys_role_menu → sys_menu.perms
 * <p>
 * 注意返回值中的通配符：Sa-Token 仅支持模式串尾部 *（如 system:* 前缀匹配），
 * 因此超级管理员返回 "*" 即可放行全部权限。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    /**
     * 超级管理员用户ID，拥有全部权限与角色
     */
    public static final Long SUPER_ADMIN_USER_ID = 1L;

    private final SystemMenuMapper menuMapper;
    private final SystemRoleMapper roleMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = toUserId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        if (SUPER_ADMIN_USER_ID.equals(userId)) {
            // "*" 通配任意权限码，超级管理员无需逐条授权
            return Collections.singletonList("*");
        }
        return menuMapper.selectPermissionStringsByUserId(userId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = toUserId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        if (SUPER_ADMIN_USER_ID.equals(userId)) {
            return Collections.singletonList("*");
        }
        return roleMapper.selectRoleKeysByUserId(userId);
    }

    private Long toUserId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        try {
            return Long.valueOf(loginId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

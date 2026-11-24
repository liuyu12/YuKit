package cn.lycoder.boot.framework.security.core.util;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 安全框架工具类：对 Sa-Token 的薄封装，业务代码通过它获取登录态，便于将来替换底层安全框架
 */
public final class SecurityFrameworkUtils {

    private SecurityFrameworkUtils() {
        throw new UnsupportedOperationException("工具类，禁止实例化");
    }

    /**
     * 获取当前登录用户 ID，未登录返回 null
     */
    public static Long getLoginUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        return loginId == null ? null : Long.valueOf(loginId.toString());
    }

    /**
     * 当前会话是否已登录
     */
    public static boolean isLoggedIn() {
        // 无 Web 上下文（如定时任务）时 getLoginIdDefaultNull 会取不到，视为未登录
        try {
            return StpUtil.isLogin();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验当前用户是否拥有指定权限
     */
    public static boolean hasPermission(String permission) {
        return StpUtil.hasPermission(permission);
    }
}

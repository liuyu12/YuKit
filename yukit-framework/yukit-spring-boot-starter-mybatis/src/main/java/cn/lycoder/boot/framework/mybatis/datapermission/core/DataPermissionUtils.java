package cn.lycoder.boot.framework.mybatis.datapermission.core;

import java.util.function.Supplier;

/**
 * 数据权限开关（线程级）
 * <p>
 * 数据权限插件对全局 SELECT 生效，但写路径的存在性/唯一性校验、内部聚合、定时任务等
 * 「不应受当前用户可见范围限制」的查询需要临时关闭。默认开启；关闭范围支持嵌套（保存并恢复旧值）。
 */
public final class DataPermissionUtils {

    private static final ThreadLocal<Boolean> DISABLED = new ThreadLocal<>();

    private DataPermissionUtils() {
        throw new UnsupportedOperationException("工具类，禁止实例化");
    }

    public static boolean isDisabled() {
        return Boolean.TRUE.equals(DISABLED.get());
    }

    /**
     * 关闭当前线程的数据权限过滤；须与 {@link #restore(boolean)} 成对使用（切面场景）
     */
    public static void disable() {
        DISABLED.set(Boolean.TRUE);
    }

    /**
     * 恢复到进入前的状态：原本已关闭则保持关闭，否则清除标记
     */
    public static void restore(boolean previousDisabled) {
        if (previousDisabled) {
            DISABLED.set(Boolean.TRUE);
        } else {
            DISABLED.remove();
        }
    }

    /**
     * 在禁用数据权限的上下文中执行，结束（含异常）后恢复原状态，支持嵌套调用
     */
    public static void executeIgnore(Runnable runnable) {
        boolean previous = isDisabled();
        DISABLED.set(Boolean.TRUE);
        try {
            runnable.run();
        } finally {
            restore(previous);
        }
    }

    public static <T> T executeIgnore(Supplier<T> supplier) {
        boolean previous = isDisabled();
        DISABLED.set(Boolean.TRUE);
        try {
            return supplier.get();
        } finally {
            restore(previous);
        }
    }
}

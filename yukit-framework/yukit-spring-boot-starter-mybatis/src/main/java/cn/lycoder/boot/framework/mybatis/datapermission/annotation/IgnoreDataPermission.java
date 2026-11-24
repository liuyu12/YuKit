package cn.lycoder.boot.framework.mybatis.datapermission.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略数据权限
 * <p>
 * 标注在方法（或类）上，其执行期间对当前线程关闭数据权限过滤，
 * 用于写路径的存在性/唯一性校验、管理员全量查看等「不应受当前用户可见范围限制」的场景。
 * 由 {@code IgnoreDataPermissionAspect} 拦截，底层用 {@code DataPermissionUtils} 的线程开关实现，支持嵌套。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreDataPermission {
}

package cn.lycoder.boot.framework.mybatis.datapermission.aop;

import cn.lycoder.boot.framework.mybatis.datapermission.core.DataPermissionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * {@code @IgnoreDataPermission} 切面：方法执行期间关闭当前线程的数据权限过滤
 * <p>
 * 保存并恢复旧状态以支持嵌套（如被忽略的方法内部又调用了普通查询）。由自动配置注册为 Bean。
 */
@Aspect
public class IgnoreDataPermissionAspect {

    @Around("@annotation(cn.lycoder.boot.framework.mybatis.datapermission.annotation.IgnoreDataPermission)"
            + " || @within(cn.lycoder.boot.framework.mybatis.datapermission.annotation.IgnoreDataPermission)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean previous = DataPermissionUtils.isDisabled();
        DataPermissionUtils.disable();
        try {
            return joinPoint.proceed();
        } finally {
            DataPermissionUtils.restore(previous);
        }
    }
}

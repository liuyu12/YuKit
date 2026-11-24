package cn.lycoder.boot.framework.mybatis.datapermission.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明某个实体对应的表参与「部门数据权限」过滤，并指定其过滤列。
 * <p>
 * 与 {@code @TableName} 一起标注在 DO 类上。框架侧只提供该声明契约，具体消费由业务规则
 * （如 module-system 的 {@code DeptDataPermissionRule}）通过扫描 MyBatis-Plus 表元数据自动登记，
 * 命中后按 {@link #deptColumn()} 追加 {@code alias.<deptColumn> IN (...)}，
 * 拥有「仅本人」范围时按 {@link #selfColumn()} 追加 {@code alias.<selfColumn> = '<当前用户>'}，两者并集（OR）。
 * <p>
 * 下沉到框架的目的：任何依赖本 starter 的模块都能在自己的实体上声明受控，而无需依赖 module-system；
 * 新增一张受控表只需加本注解（并按需调整列名），无需改动集中的规则类（开闭原则）。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeptDataPermission {

    /**
     * 部门过滤列名，默认 {@code dept_id}
     */
    String deptColumn() default "dept_id";

    /**
     * 「仅本人」过滤列（归属人/创建者列）名，默认 {@code create_by}（BaseEntity 审计字段，值为登录用户 id 字符串）
     */
    String selfColumn() default "create_by";

}

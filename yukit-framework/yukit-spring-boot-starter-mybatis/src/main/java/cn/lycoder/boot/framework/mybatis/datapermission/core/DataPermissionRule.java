package cn.lycoder.boot.framework.mybatis.datapermission.core;

import net.sf.jsqlparser.expression.Expression;

/**
 * 数据权限规则 SPI
 * <p>
 * 框架层只定义「如何为某张表产出过滤条件」的契约，不关心具体业务表与算法；
 * 由业务模块（如 module-system 的部门数据权限）实现并注册为 Bean，
 * {@link cn.lycoder.boot.framework.mybatis.datapermission.core.YukitDataPermissionHandler}
 * 会在每条 SELECT 的表上回调全部规则，把非 null 表达式 AND 合并进 WHERE。
 * <p>
 * 约定：命中「无需过滤」（表不参与、超级管理员、无登录态等）返回 {@code null}，
 * 需要限制则返回一个 JSqlParser {@link Expression}（如 {@code alias.dept_id IN (...)}）。
 */
public interface DataPermissionRule {

    /**
     * 针对某个表引用产出过滤条件。
     *
     * @param tableName  表名（可能带库前缀，需自行归一化）
     * @param tableAlias 表别名，无别名为 null；拼列引用时应优先使用别名以避免连表歧义
     * @return 过滤表达式；该表不参与过滤则返回 {@code null}
     */
    Expression getExpression(String tableName, String tableAlias);
}

package cn.lycoder.boot.framework.mybatis.datapermission.core;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 数据权限处理器：桥接 MyBatis-Plus 的 {@link MultiDataPermissionHandler} 与项目自定义的 {@link DataPermissionRule}
 * <p>
 * 采用 MP 新版「多表」处理器：{@code DataPermissionInterceptor} 解析 SELECT/UPDATE/DELETE 后，<b>逐表</b>回调
 * {@link #getSqlSegment(Table, Expression, String)}，本类把该表交给全部 {@link DataPermissionRule}，
 * 将非 null 条件 AND 合并后返回，MP 会把返回的条件<b>追加</b>（而非覆盖）到该表所属查询的 WHERE。
 * <p>
 * 因此命中「无需过滤」（线程关闭数据权限、表不参与、超级管理员、无登录态等）返回 {@code null} 即可，安全。
 * <p>
 * 规则通过 {@link Supplier} <b>懒解析</b>：装配 {@code MybatisPlusInterceptor} 时不能实例化规则，
 * 否则「规则→Mapper→SqlSessionFactory→拦截器→规则」会成环；故首次查询时才解析并缓存。
 */
public class YukitDataPermissionHandler implements MultiDataPermissionHandler {

    private final Supplier<List<DataPermissionRule>> rulesSupplier;

    /**
     * 规则缓存：用线程安全类型 {@link AtomicReference} 承载，配合 {@link #rules()} 的 CAS 完成懒解析，
     * 避免 volatile 引用「读取-判断-赋值」非原子及可变状态共享问题；缓存的列表来自 {@code Stream#toList()}，不可变可安全共享。
     */
    private final AtomicReference<List<DataPermissionRule>> resolvedRules = new AtomicReference<>();

    /**
     * 生产用：懒解析规则（避免与 SqlSessionFactory 形成创建环）
     */
    public YukitDataPermissionHandler(Supplier<List<DataPermissionRule>> rulesSupplier) {
        this.rulesSupplier = rulesSupplier;
    }

    /**
     * 测试用：直接给定规则集合
     */
    public YukitDataPermissionHandler(List<DataPermissionRule> rules) {
        this(() -> rules);
    }

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        // 线程级关闭（写路径校验 / @IgnoreDataPermission）：返回 null 表示不追加条件，原查询不受影响
        if (DataPermissionUtils.isDisabled()) {
            return null;
        }
        String tableName = table.getName();
        String tableAlias = table.getAlias() != null ? table.getAlias().getName() : null;

        Expression condition = null;
        for (DataPermissionRule rule : rules()) {
            Expression expression = rule.getExpression(tableName, tableAlias);
            if (expression == null) {
                continue;
            }
            condition = condition == null ? expression : new AndExpression(condition, expression);
        }
        return condition;
    }

    private List<DataPermissionRule> rules() {
        // 已缓存则直接返回（不触发 supplier）；并发首次解析时 CAS 保证仅一份胜出并被复用
        return resolvedRules.updateAndGet(current -> current != null ? current : rulesSupplier.get());
    }
}

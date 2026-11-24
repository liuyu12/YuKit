package cn.lycoder.boot.framework.mybatis.datapermission.core;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link YukitDataPermissionHandler} 单元测试：线程开关、逐规则求值与 AND 合并、别名透传
 * <p>
 * 纯 Mockito，不起 Spring 上下文；规则用 mock，表达式用 JSqlParser 真实解析以保证语义。
 */
class YukitDataPermissionHandlerTest {

    private final DataPermissionRule ruleA = mock(DataPermissionRule.class);
    private final DataPermissionRule ruleB = mock(DataPermissionRule.class);

    private static Table table(String name, String alias) {
        Table table = new Table(name);
        if (alias != null) {
            table.setAlias(new Alias(alias));
        }
        return table;
    }

    @Test
    @DisplayName("线程关闭数据权限：返回 null，且不回调任何规则")
    void disabled_shouldReturnNullWithoutCallingRules() {
        YukitDataPermissionHandler handler = new YukitDataPermissionHandler(List.of(ruleA));
        Expression result = DataPermissionUtils.executeIgnore(
                () -> handler.getSqlSegment(table("sys_user", null), null, "ms"));
        assertThat(result).isNull();
        verify(ruleA, never()).getExpression(any(), any());
    }

    @Test
    @DisplayName("规则返回 null：整体不改写，返回 null")
    void ruleReturnsNull_shouldNotAppend() {
        when(ruleA.getExpression("sys_user", null)).thenReturn(null);
        YukitDataPermissionHandler handler = new YukitDataPermissionHandler(List.of(ruleA));

        assertThat(handler.getSqlSegment(table("sys_user", null), null, "ms")).isNull();
    }

    @Test
    @DisplayName("单规则命中：直接返回该条件")
    void singleRule_shouldReturnItsCondition() throws Exception {
        Expression cond = CCJSqlParserUtil.parseCondExpression("sys_user.dept_id IN (105)");
        when(ruleA.getExpression("sys_user", null)).thenReturn(cond);
        YukitDataPermissionHandler handler = new YukitDataPermissionHandler(List.of(ruleA));

        Expression result = handler.getSqlSegment(table("sys_user", null), null, "ms");
        assertThat(result).isSameAs(cond);
    }

    @Test
    @DisplayName("多规则命中：以 AND 合并两条条件")
    void multipleRules_shouldAndMerge() {
        YukitDataPermissionHandler handler = new YukitDataPermissionHandler(List.of(ruleA, ruleB));
        try {
            when(ruleA.getExpression("sys_user", null)).thenReturn(CCJSqlParserUtil.parseCondExpression("a = 1"));
            when(ruleB.getExpression("sys_user", null)).thenReturn(CCJSqlParserUtil.parseCondExpression("b = 2"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Expression result = handler.getSqlSegment(table("sys_user", null), null, "ms");
        assertThat(result).isInstanceOf(AndExpression.class);
        assertThat(result.toString().replaceAll("\\s+", " ")).isEqualTo("a = 1 AND b = 2");
    }

    @Test
    @DisplayName("表别名透传给规则；无别名时为 null")
    void shouldPassTableNameAndAlias() {
        YukitDataPermissionHandler handler = new YukitDataPermissionHandler(List.of(ruleA));
        handler.getSqlSegment(table("sys_user", "u"), null, "ms");
        verify(ruleA).getExpression("sys_user", "u");

        handler.getSqlSegment(table("sys_user", null), null, "ms");
        verify(ruleA).getExpression("sys_user", null);
    }
}

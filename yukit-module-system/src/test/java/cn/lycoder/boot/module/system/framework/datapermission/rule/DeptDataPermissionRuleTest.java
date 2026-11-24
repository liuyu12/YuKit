package cn.lycoder.boot.module.system.framework.datapermission.rule;

import cn.lycoder.boot.framework.security.core.util.SecurityFrameworkUtils;
import cn.lycoder.boot.module.system.dal.dataobject.role.SystemRoleDO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.role.SystemRoleMapper;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import cn.lycoder.boot.module.system.service.dept.DeptService;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * {@link DeptDataPermissionRule} 单元测试：各 data_scope 下产出的 JSqlParser 条件表达式
 * <p>
 * 断言用表达式的字符串形态（去除多余空白后比较），因可见部门来自集合、IN 内顺序不定，
 * 对多元素场景改用「包含列名 + 包含各部门」的方式，避免顺序断言脆弱。
 */
class DeptDataPermissionRuleTest {

    private final SystemRoleMapper roleMapper = mock(SystemRoleMapper.class);
    private final SystemUserMapper userMapper = mock(SystemUserMapper.class);
    private final DeptService deptService = mock(DeptService.class);

    private final DeptDataPermissionRule rule =
            new DeptDataPermissionRule(roleMapper, userMapper, deptService);

    /** 在指定登录态下求值 sys_user 表（无别名） */
    private Expression evalUser(Long loginUserId) {
        try (MockedStatic<SecurityFrameworkUtils> st = mockStatic(SecurityFrameworkUtils.class)) {
            st.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(loginUserId);
            return rule.getExpression("sys_user", null);
        }
    }

    @Test
    @DisplayName("非受控表（sys_role）：返回 null 不改写")
    void nonControlledTable_shouldReturnNull() {
        try (MockedStatic<SecurityFrameworkUtils> st = mockStatic(SecurityFrameworkUtils.class)) {
            st.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(2L);
            assertThat(rule.getExpression("sys_role", null)).isNull();
        }
    }

    @Test
    @DisplayName("无登录态：返回 null 放行（不破坏登录/内部流程）")
    void notLogin_shouldReturnNull() {
        assertThat(evalUser(null)).isNull();
    }

    @Test
    @DisplayName("超级管理员(userId=1)：返回 null 放行全部")
    void superAdmin_shouldReturnNull() {
        assertThat(evalUser(1L)).isNull();
    }

    @Test
    @DisplayName("自定(CUSTOM)：sys_user.dept_id IN 勾选部门")
    void custom_shouldBuildInWithRoleDepts() {
        when(roleMapper.selectListByUserId(2L)).thenReturn(List.of(role(10L, "2")));
        when(roleMapper.selectDeptIdsByRoleId(10L)).thenReturn(List.of(100L, 101L, 105L));

        Expression expression = evalUser(2L);

        String sql = normalize(expression);
        assertThat(sql).startsWith("sys_user.dept_id IN (");
        assertThat(sql).contains("100", "101", "105");
    }

    @Test
    @DisplayName("本部门(DEPT)：仅本人部门")
    void dept_shouldUseOwnDept() {
        when(roleMapper.selectListByUserId(2L)).thenReturn(List.of(role(10L, "3")));
        when(userMapper.selectById(2L)).thenReturn(user(2L, 105L));

        assertThat(normalize(evalUser(2L))).isEqualTo("sys_user.dept_id IN (105)");
    }

    @Test
    @DisplayName("本部门及以下(DEPT_AND_CHILD)：含子孙部门")
    void deptAndChild_shouldIncludeChildren() {
        when(roleMapper.selectListByUserId(2L)).thenReturn(List.of(role(10L, "4")));
        when(userMapper.selectById(2L)).thenReturn(user(2L, 101L));
        when(deptService.getSelfAndChildDeptIds(101L)).thenReturn(List.of(101L, 103L, 104L));

        String sql = normalize(evalUser(2L));
        assertThat(sql).startsWith("sys_user.dept_id IN (");
        assertThat(sql).contains("101", "103", "104");
    }

    @Test
    @DisplayName("含全部(ALL)角色：返回 null 放行")
    void anyAllRole_shouldReturnNull() {
        when(roleMapper.selectListByUserId(2L)).thenReturn(List.of(role(10L, "4"), role(11L, "1")));
        assertThat(evalUser(2L)).isNull();
    }

    @Test
    @DisplayName("无任何角色：可见集合为空 → 恒假 1 = 0")
    void noRoles_shouldBuildAlwaysFalse() {
        when(roleMapper.selectListByUserId(2L)).thenReturn(List.of());
        assertThat(normalize(evalUser(2L))).isEqualTo("1 = 0");
    }

    @Test
    @DisplayName("存在别名：列引用使用别名以避免连表歧义")
    void withAlias_shouldUseAliasPrefix() {
        when(roleMapper.selectListByUserId(2L)).thenReturn(List.of(role(10L, "3")));
        when(userMapper.selectById(2L)).thenReturn(user(2L, 105L));

        Expression expression;
        try (MockedStatic<SecurityFrameworkUtils> st = mockStatic(SecurityFrameworkUtils.class)) {
            st.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(2L);
            expression = rule.getExpression("sys_user", "u");
        }
        assertThat(normalize(expression)).isEqualTo("u.dept_id IN (105)");
    }

    @Test
    @DisplayName("仅本人(SELF)：sys_user.create_by = 当前用户")
    void self_shouldBuildCreateByEquals() {
        when(roleMapper.selectListByUserId(2L)).thenReturn(List.of(role(10L, "5")));
        assertThat(normalize(evalUser(2L))).isEqualTo("sys_user.create_by = '2'");
    }

    @Test
    @DisplayName("自定 + 仅本人：部门范围与 create_by 以 OR 并集组合")
    void customPlusSelf_shouldOrUnion() {
        when(roleMapper.selectListByUserId(2L)).thenReturn(List.of(role(10L, "2"), role(11L, "5")));
        when(roleMapper.selectDeptIdsByRoleId(10L)).thenReturn(List.of(105L));

        String sql = normalize(evalUser(2L));
        assertThat(sql).contains("sys_user.dept_id IN (105)");
        assertThat(sql).contains("sys_user.create_by = '2'");
        assertThat(sql).contains(" OR ");
    }

    @Test
    @DisplayName("本部门 + 仅本人：部门与 create_by 并集")
    void deptPlusSelf_shouldOrUnion() {
        when(roleMapper.selectListByUserId(2L)).thenReturn(List.of(role(10L, "3"), role(11L, "5")));
        when(userMapper.selectById(2L)).thenReturn(user(2L, 105L));

        String sql = normalize(evalUser(2L));
        assertThat(sql).contains("sys_user.dept_id IN (105)");
        assertThat(sql).contains("sys_user.create_by = '2'");
        assertThat(sql).contains(" OR ");
    }

    private String normalize(Expression expression) {
        assertThat(expression).isNotNull();
        // 归一化空白，兼容 JSqlParser 不同版本对 IN 列表/等号周围的空格排版
        return expression.toString().replaceAll("\\s+", " ").trim();
    }

    private SystemRoleDO role(Long roleId, String dataScope) {
        SystemRoleDO role = new SystemRoleDO();
        role.setRoleId(roleId);
        role.setDataScope(dataScope);
        return role;
    }

    private SystemUserDO user(Long userId, Long deptId) {
        SystemUserDO user = new SystemUserDO();
        user.setUserId(userId);
        user.setDeptId(deptId);
        return user;
    }
}

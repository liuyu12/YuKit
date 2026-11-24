package cn.lycoder.boot.module.system.framework.datapermission.rule;

import cn.lycoder.boot.framework.mybatis.datapermission.core.DataPermissionRule;
import cn.lycoder.boot.framework.security.core.util.SecurityFrameworkUtils;
import cn.lycoder.boot.module.system.dal.dataobject.role.SystemRoleDO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.role.SystemRoleMapper;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import cn.lycoder.boot.module.system.framework.datapermission.core.DataScopeType;
import cn.lycoder.boot.module.system.framework.datapermission.core.DeptDataPermissionTableResolver;
import cn.lycoder.boot.module.system.framework.datapermission.core.DeptDataScope;
import cn.lycoder.boot.module.system.framework.security.StpInterfaceImpl;
import cn.lycoder.boot.module.system.service.dept.DeptService;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门数据权限规则：对受控表按其 {@code dept_id} 追加可见范围条件
 * <p>
 * 作为 {@link DataPermissionRule} 的业务实现注册到框架，由 MP {@code DataPermissionInterceptor} 逐表回调。
 * 受控表及其部门过滤列不再集中硬编码，而是由实体上的 {@code @DeptDataPermission} 声明、
 * {@link DeptDataPermissionTableResolver} 运行时扫描 MP 表元数据自动登记（新增受控表无需改动本类）。
 * <p>
 * 放行（返回 null，不改写）的情形：非受控表、无登录态（登录链/内部任务/定时任务等，避免误伤全局流程）、
 * 超级管理员、拥有「全部数据权限」角色。需要限制时：按可见部门生成 {@code alias.<deptColumn> IN (...)}，
 * 拥有「仅本人」范围时并上 {@code alias.<selfColumn> = '<当前用户>'}，两者以并集（OR）组合；均无则恒假 {@code 1 = 0}。
 * <p>
 * 部门列与「仅本人」列由实体上的 {@code @DeptDataPermission} 声明（默认 {@code dept_id}/{@code create_by}），
 * 列引用优先用表别名以避免连表歧义；部门 id 与本人列值（登录用户 id）均为数字串，无注入风险。
 */
@Component
@RequiredArgsConstructor
public class DeptDataPermissionRule implements DataPermissionRule {

    private static final String ALWAYS_FALSE = "1 = 0";

    private final SystemRoleMapper roleMapper;
    private final SystemUserMapper userMapper;
    private final DeptService deptService;
    private final DeptDataPermissionTableResolver tableResolver;

    @Override
    public Expression getExpression(String tableName, String tableAlias) {
        DeptDataPermissionTableResolver.DeptColumns columns = tableResolver.findColumns(tableName);
        if (columns == null) {
            return null;
        }
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        // 无登录态：无法确定可见范围，放行（不影响登录、内部/定时流程）
        if (userId == null) {
            return null;
        }
        // 超级管理员放行全部
        if (StpInterfaceImpl.SUPER_ADMIN_USER_ID.equals(userId)) {
            return null;
        }
        DeptDataScope scope = calculate(userId);
        if (scope.isIgnoreAll()) {
            return null;
        }
        return buildCondition(tableAlias, normalizeTable(tableName), columns, userId, scope);
    }

    /**
     * 组装该表的过滤条件：部门范围与「仅本人」以并集（OR）组合；两者皆空则恒假。
     * <p>
     * 多角色下，任一角色授予的部门范围或仅本人都会并入，OR 组合后整体括起，由 MP 追加到该表 WHERE。
     */
    private Expression buildCondition(String tableAlias, String normalizedTable, DeptDataPermissionTableResolver.DeptColumns columns, Long userId, DeptDataScope scope) {
        String aliasRef = isBlank(tableAlias) ? normalizedTable : tableAlias;
        List<String> parts = new ArrayList<>();
        if (!scope.getDeptIds().isEmpty()) {
            String in = scope.getDeptIds().stream().map(String::valueOf).collect(Collectors.joining(", "));
            parts.add(aliasRef + "." + columns.deptColumn() + " IN (" + in + ")");
        }
        if (scope.isSelf()) {
            parts.add(aliasRef + "." + columns.selfColumn() + " = '" + userId + "'");
        }
        if (parts.isEmpty()) {
            return parse(ALWAYS_FALSE);
        }
        if (parts.size() == 1) {
            return parse(parts.getFirst());
        }
        return parse("(" + String.join(" OR ", parts) + ")");
    }

    /**
     * 依据当前登录用户角色的 data_scope 计算可见范围（多角色取并集，含「全部」则整体放行）
     */
    private DeptDataScope calculate(Long userId) {
        List<SystemRoleDO> roles = roleMapper.selectListByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return DeptDataScope.none();
        }
        Scope scope = new Scope();
        for (SystemRoleDO role : roles) {
            // 命中「全部数据权限」则整体放行，无需继续累加其它角色
            if (collectScope(role, userId, scope)) {
                return DeptDataScope.all();
            }
        }
        return DeptDataScope.of(scope.deptIds, scope.self);
    }

    /**
     * 按角色的 data_scope 累加其可见范围到 {@code scope}。
     *
     * @return {@code true} 表示该角色拥有「全部数据权限」，调用方应整体放行（ignoreAll）
     */
    private boolean collectScope(SystemRoleDO role, Long userId, Scope scope) {
        DataScopeType type = DataScopeType.parse(role.getDataScope());
        if (type == null) {
            return false;
        }
        switch (type) {
            case ALL -> {
                return true;
            }
            case CUSTOM -> {
                List<Long> ids = roleMapper.selectDeptIdsByRoleId(role.getRoleId());
                if (ids != null) {
                    scope.deptIds.addAll(ids);
                }
            }
            case DEPT -> {
                Long selfDeptId = currentDeptId(userId);
                if (selfDeptId != null) {
                    scope.deptIds.add(selfDeptId);
                }
            }
            case DEPT_AND_CHILD -> {
                Long selfDeptId = currentDeptId(userId);
                if (selfDeptId != null) {
                    scope.deptIds.addAll(deptService.getSelfAndChildDeptIds(selfDeptId));
                }
            }
            case SELF -> scope.self = true;
        }
        return false;
    }

    /**
     * 多角色范围累加的中间态：部门集合与「仅本人」标记
     */
    private static final class Scope {
        private final Set<Long> deptIds = new HashSet<>();
        private boolean self;
    }

    private Long currentDeptId(Long userId) {
        SystemUserDO user = userMapper.selectById(userId);
        return user == null ? null : user.getDeptId();
    }

    private static Expression parse(String condition) {
        try {
            return CCJSqlParserUtil.parseCondExpression(condition);
        } catch (Exception e) {
            // 生成的条件仅含数字，正常不会失败；异常时以恒假兜底，避免数据越权泄露
            throw new IllegalStateException("数据权限条件解析失败: " + condition, e);
        }
    }

    private static String normalizeTable(String tableName) {
        if (tableName == null) {
            return "";
        }
        String name = tableName.replace("`", "").toLowerCase();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

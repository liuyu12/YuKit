package cn.lycoder.boot.module.system.framework.datapermission.core;

import lombok.RequiredArgsConstructor;

/**
 * 数据范围类型，取值与 sys_role.data_scope 字段对应
 */
@RequiredArgsConstructor
public enum DataScopeType {

    /**
     * 全部数据权限
     */
    ALL("1"),
    /**
     * 自定数据权限（按 sys_role_dept 勾选的部门）
     */
    CUSTOM("2"),
    /**
     * 本部门数据权限
     */
    DEPT("3"),
    /**
     * 本部门及以下数据权限
     */
    DEPT_AND_CHILD("4"),
    /**
     * 仅本人数据权限（只能看到自己创建的记录，按 BaseEntity 的 create_by 过滤）
     */
    SELF("5");

    private final String scope;

    /**
     * 按 data_scope 字符串解析，未知值返回 null（视为不参与计算）
     */
    public static DataScopeType parse(String scope) {
        if (scope == null) {
            return null;
        }
        for (DataScopeType type : values()) {
            if (type.scope.equals(scope)) {
                return type;
            }
        }
        return null;
    }
}

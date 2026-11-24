package cn.lycoder.boot.module.system.framework.datapermission.core;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;

/**
 * 数据范围计算结果
 * <p>
 * {@code ignoreAll=true} 表示不受限制（超级管理员或拥有「全部数据权限」角色）；
 * 否则可见 {@code deptIds} 部门的数据，并（当 {@code self=true}）或上本人创建的数据；
 * 两者皆空表示看不到任何数据。
 */
@Getter
public class DeptDataScope {

    private final boolean ignoreAll;
    private final Set<Long> deptIds;
    /**
     * 是否包含「仅本人」范围（命中则额外可见自己创建的记录）
     */
    private final boolean self;

    private DeptDataScope(boolean ignoreAll, Set<Long> deptIds, boolean self) {
        this.ignoreAll = ignoreAll;
        this.deptIds = deptIds;
        this.self = self;
    }

    /**
     * 全部数据权限，不做过滤
     */
    public static DeptDataScope all() {
        return new DeptDataScope(true, Collections.emptySet(), false);
    }

    /**
     * 指定可见部门集合
     */
    public static DeptDataScope of(Set<Long> deptIds) {
        return of(deptIds, false);
    }

    /**
     * 指定可见部门集合，并可选叠加「仅本人」
     */
    public static DeptDataScope of(Set<Long> deptIds, boolean self) {
        return new DeptDataScope(false, deptIds == null ? Collections.emptySet() : deptIds, self);
    }

    /**
     * 无任何可见部门
     */
    public static DeptDataScope none() {
        return new DeptDataScope(false, Collections.emptySet(), false);
    }
}

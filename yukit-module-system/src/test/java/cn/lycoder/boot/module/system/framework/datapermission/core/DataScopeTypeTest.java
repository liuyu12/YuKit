package cn.lycoder.boot.module.system.framework.datapermission.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DataScopeType} 解析与 {@link DeptDataScope} 语义单元测试
 */
class DataScopeTypeTest {

    @Test
    @DisplayName("parse：映射 1-5，未知与 null 返回 null")
    void parse() {
        assertThat(DataScopeType.parse("1")).isEqualTo(DataScopeType.ALL);
        assertThat(DataScopeType.parse("2")).isEqualTo(DataScopeType.CUSTOM);
        assertThat(DataScopeType.parse("3")).isEqualTo(DataScopeType.DEPT);
        assertThat(DataScopeType.parse("4")).isEqualTo(DataScopeType.DEPT_AND_CHILD);
        assertThat(DataScopeType.parse("5")).isEqualTo(DataScopeType.SELF);
        assertThat(DataScopeType.parse("9")).isNull();
        assertThat(DataScopeType.parse(null)).isNull();
    }

    @Test
    @DisplayName("DeptDataScope：all 放行、none 空集合、of 保留部门、of+self 叠加仅本人")
    void deptDataScope() {
        assertThat(DeptDataScope.all().isIgnoreAll()).isTrue();

        DeptDataScope none = DeptDataScope.none();
        assertThat(none.isIgnoreAll()).isFalse();
        assertThat(none.getDeptIds()).isEmpty();
        assertThat(none.isSelf()).isFalse();

        DeptDataScope of = DeptDataScope.of(java.util.Set.of(1L, 2L));
        assertThat(of.isIgnoreAll()).isFalse();
        assertThat(of.getDeptIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(of.isSelf()).isFalse();

        DeptDataScope withSelf = DeptDataScope.of(java.util.Set.of(3L), true);
        assertThat(withSelf.isSelf()).isTrue();
        assertThat(withSelf.getDeptIds()).containsExactly(3L);
    }
}

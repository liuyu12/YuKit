package cn.lycoder.boot.module.system.framework.datapermission.core;

import cn.lycoder.boot.framework.mybatis.datapermission.annotation.DeptDataPermission;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 部门数据权限受控表登记表：以「实体注解」为唯一事实来源，自动构建 {@code 表名 -> 过滤列} 映射。
 * <p>
 * 懒解析：首次访问时扫描 MyBatis-Plus 已初始化的全部 {@link TableInfo}，挑出实体上标注了
 * {@link DeptDataPermission} 的表并缓存为不可变映射。之所以懒加载而非构造期扫描，是因为
 * {@code TableInfoHelper} 的注册表在 {@code SqlSessionFactory} 构建完成后才填充，而规则装配阶段
 * 不能依赖它（否则与数据权限拦截器形成创建环）。
 * <p>
 * 新增一张受控表只需给其实体加 {@link DeptDataPermission}（可按需调整部门列/本人列），无需改动本类与规则类。
 */
@Component
public class DeptDataPermissionTableResolver {

    private final AtomicReference<Map<String, DeptColumns>> deptColumns = new AtomicReference<>();

    /**
     * 生产用：运行时扫描 MP 表元数据懒构建
     */
    @Autowired
    public DeptDataPermissionTableResolver() {
    }

    /**
     * 测试用：直接给定受控表 -> 过滤列映射（表名会归一化）
     */
    DeptDataPermissionTableResolver(Map<String, DeptColumns> fixedColumns) {
        Map<String, DeptColumns> normalized = new HashMap<>();
        fixedColumns.forEach((table, columns) -> normalized.put(normalize(table), columns));
        this.deptColumns.set(Map.copyOf(normalized));
    }

    /**
     * 查询某表是否为受控表及其过滤列。
     *
     * @param tableName 表名（可能带库前缀/反引号，内部归一化）
     * @return 过滤列；该表不参与部门数据权限则返回 {@code null}
     */
    public DeptColumns findColumns(String tableName) {
        if (tableName == null) {
            return null;
        }
        return deptColumns().get(normalize(tableName));
    }

    private Map<String, DeptColumns> deptColumns() {
        // 已缓存则直接返回（不触发扫描）；并发首次解析时 CAS 保证仅一份胜出并被复用
        return deptColumns.updateAndGet(current -> current != null ? current : scan());
    }

    private static Map<String, DeptColumns> scan() {
        Map<String, DeptColumns> result = new HashMap<>();
        for (TableInfo tableInfo : TableInfoHelper.getTableInfos()) {
            DeptDataPermission annotation = tableInfo.getEntityType().getAnnotation(DeptDataPermission.class);
            if (annotation != null) {
                result.put(normalize(tableInfo.getTableName()),
                        new DeptColumns(annotation.deptColumn(), annotation.selfColumn()));
            }
        }
        return Map.copyOf(result);
    }

    private static String normalize(String tableName) {
        String name = tableName.replace("`", "").toLowerCase();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }

    /**
     * 受控表的数据权限过滤列：部门列 + 「仅本人」（归属人）列。
     */
    public record DeptColumns(String deptColumn, String selfColumn) {
    }

}

package cn.lycoder.boot.module.system.dal.mapper.dept;

import cn.lycoder.boot.module.system.dal.dataobject.dept.SystemDeptDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门 Mapper
 * <p>
 * 单表 CRUD / 树查询用 MyBatis-Plus 条件构造器；子孙 ancestors 批量前缀替换用 XML
 */
public interface SystemDeptMapper extends BaseMapper<SystemDeptDO> {

    /**
     * 查询某部门的全部子孙部门 ID（不含自身），基于 ancestors 祖级链匹配
     */
    List<Long> selectChildDeptIds(@Param("deptId") Long deptId);

    /**
     * 父部门变更时，按祖级前缀替换级联刷新所有子孙的 ancestors
     */
    int updateDeptChildren(@Param("oldAncestors") String oldAncestors,
                           @Param("newAncestors") String newAncestors,
                           @Param("deptId") Long deptId);
}

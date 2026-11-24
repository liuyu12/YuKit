package cn.lycoder.boot.module.system.dal.mapper.role;

import cn.lycoder.boot.module.system.dal.dataobject.role.SystemRoleDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 角色 Mapper
 * <p>
 * 自定义 SQL 见 resources/mapper/role/SystemRoleMapper.xml
 */
public interface SystemRoleMapper extends BaseMapper<SystemRoleDO> {

    /**
     * 查询用户拥有的角色标识（role_key），供 Sa-Token 角色校验使用
     */
    List<String> selectRoleKeysByUserId(Long userId);

    /**
     * 查询用户拥有的完整角色（含 data_scope），供数据范围切面计算可见部门
     */
    List<SystemRoleDO> selectListByUserId(Long userId);

    /**
     * 查询角色「自定数据权限」勾选的部门 ID（sys_role_dept）
     */
    List<Long> selectDeptIdsByRoleId(Long roleId);
}

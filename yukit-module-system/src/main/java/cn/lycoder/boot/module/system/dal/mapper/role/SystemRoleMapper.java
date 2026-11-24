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
}

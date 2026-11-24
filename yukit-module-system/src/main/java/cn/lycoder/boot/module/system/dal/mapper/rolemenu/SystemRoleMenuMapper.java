package cn.lycoder.boot.module.system.dal.mapper.rolemenu;

import cn.lycoder.boot.module.system.dal.dataobject.rolemenu.SystemRoleMenuDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色和菜单关联 Mapper
 * <p>
 * 自定义 SQL 见 resources/mapper/rolemenu/SystemRoleMenuMapper.xml
 */
public interface SystemRoleMenuMapper extends BaseMapper<SystemRoleMenuDO> {

    /**
     * 删除某角色的全部菜单授权
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色菜单授权
     */
    int insertBatch(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    /**
     * 查询某角色已授权的菜单ID集合
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}

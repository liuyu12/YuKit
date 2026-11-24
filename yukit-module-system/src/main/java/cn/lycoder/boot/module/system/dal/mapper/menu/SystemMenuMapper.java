package cn.lycoder.boot.module.system.dal.mapper.menu;

import cn.lycoder.boot.module.system.dal.dataobject.menu.SystemMenuDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 菜单 Mapper
 * <p>
 * 自定义 SQL 见 resources/mapper/menu/SystemMenuMapper.xml
 */
public interface SystemMenuMapper extends BaseMapper<SystemMenuDO> {

    /**
     * 查询用户拥有的权限标识（perms），链路：用户 -> 角色 -> 菜单
     * <p>
     * 供 Sa-Token StpInterface 权限校验使用
     */
    List<String> selectPermissionStringsByUserId(Long userId);
}

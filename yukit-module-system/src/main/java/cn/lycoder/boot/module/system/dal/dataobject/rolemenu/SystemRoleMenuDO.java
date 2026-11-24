package cn.lycoder.boot.module.system.dal.dataobject.rolemenu;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 角色和菜单关联 DO，对应表 sys_role_menu
 * <p>
 * 联合主键（role_id, menu_id），无独立自增主键与审计字段，故不继承 BaseEntity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role_menu")
public class SystemRoleMenuDO implements Serializable {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;
}

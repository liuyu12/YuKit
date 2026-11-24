package cn.lycoder.boot.module.system.dal.dataobject.menu;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单权限 DO，对应表 sys_menu
 * <p>
 * 说明：sys_menu 无 del_flag 字段，采用物理删除，故不覆盖 @TableLogic
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SystemMenuDO extends BaseEntity {

    /**
     * 根菜单父节点 ID
     */
    public static final Long ROOT_PARENT_ID = 0L;

    /**
     * 菜单类型 - 目录
     */
    public static final String TYPE_DIRECTORY = "M";
    /**
     * 菜单类型 - 菜单
     */
    public static final String TYPE_MENU = "C";
    /**
     * 菜单类型 - 按钮
     */
    public static final String TYPE_BUTTON = "F";

    /**
     * 菜单ID
     */
    @TableId
    private Long menuId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 路由参数
     */
    private String query;

    /**
     * 路由名称
     */
    private String routeName;

    /**
     * 是否为外链（0是 1否）
     */
    private Integer isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    private Integer isCache;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    private String menuType;

    /**
     * 菜单状态-显示状态（0显示 1隐藏）
     */
    private String visible;

    /**
     * 菜单状态（0正常 1停用）
     */
    private String status;

    /**
     * 权限标识，如 system:role:create，供 @SaCheckPermission 使用
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 备注
     */
    private String remark;
}

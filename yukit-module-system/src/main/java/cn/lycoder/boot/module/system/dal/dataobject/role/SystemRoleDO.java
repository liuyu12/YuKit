package cn.lycoder.boot.module.system.dal.dataobject.role;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色信息 DO，对应表 sys_role
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SystemRoleDO extends BaseEntity {

    /**
     * 角色状态 - 正常
     */
    public static final String STATUS_ENABLE = "0";
    /**
     * 角色状态 - 停用
     */
    public static final String STATUS_DISABLE = "1";

    /**
     * 角色ID
     */
    @TableId
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限字符串，如 admin、common，供 @SaCheckRole 使用
     */
    private String roleKey;

    /**
     * 显示顺序
     */
    private Integer roleSort;

    /**
     * 数据范围（1全部 2自定义 3本部门 4本部门及以下）
     */
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示
     */
    private Boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示
     */
    private Boolean deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic(value = "0", delval = "2")
    private String delFlag;

    /**
     * 备注
     */
    private String remark;
}

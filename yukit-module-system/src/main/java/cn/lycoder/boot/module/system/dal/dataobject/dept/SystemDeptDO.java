package cn.lycoder.boot.module.system.dal.dataobject.dept;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门 DO，对应表 sys_dept
 * <p>
 * 树形结构：parent_id 指向父部门，ancestors 存储祖级 ID 链（逗号分隔，如 "0,100,101"），
 * 便于用 find_in_set 一次查出某部门的全部子孙。数据范围权限（本部门及以下）依赖该字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SystemDeptDO extends BaseEntity {

    /**
     * 顶级部门的父 ID
     */
    public static final Long ROOT_PARENT_ID = 0L;

    /**
     * 顶级部门的祖级列表
     */
    public static final String ROOT_ANCESTORS = "0";

    /**
     * 部门状态 - 正常
     */
    public static final String STATUS_ENABLE = "0";
    /**
     * 部门状态 - 停用
     */
    public static final String STATUS_DISABLE = "1";

    /**
     * 部门ID
     */
    @TableId
    private Long deptId;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 祖级列表（逗号分隔的父链，不含自身）
     */
    private String ancestors;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic(value = "0", delval = "2")
    private String delFlag;
}

package cn.lycoder.boot.module.system.dal.dataobject.post;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位 DO，对应表 sys_post
 * <p>
 * 公司维度全局岗位（不挂部门）；status 取值由字典 sys_normal_disable 约束（@InDict 校验）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class SystemPostDO extends BaseEntity {

    /**
     * 状态 - 正常（下拉可选）
     */
    public static final String STATUS_NORMAL = "0";

    /**
     * 岗位主键
     */
    @TableId
    private Long postId;

    /**
     * 岗位编码（全局唯一，uk_sys_post_code）
     */
    private String postCode;

    /**
     * 岗位名称（业务层查重）
     */
    private String postName;

    /**
     * 岗位类别编码（预留分组维度）
     */
    private String postCategory;

    /**
     * 显示顺序
     */
    private Integer postSort;

    /**
     * 状态：0 正常 / 1 停用（字典 sys_normal_disable）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}

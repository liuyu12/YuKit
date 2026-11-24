package cn.lycoder.boot.module.system.dal.dataobject.dict;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据 DO，对应表 sys_dict_data
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SystemDictDataDO extends BaseEntity {

    /**
     * 是否默认 - 是
     */
    public static final String DEFAULT_YES = "Y";
    /**
     * 是否默认 - 否
     */
    public static final String DEFAULT_NO = "N";

    /**
     * 字典编码（主键）
     */
    @TableId
    private Long dictCode;

    /**
     * 字典排序
     */
    private Integer dictSort;

    /**
     * 字典标签（展示文本）
     */
    private String dictLabel;

    /**
     * 字典键值（存储值）
     */
    private String dictValue;

    /**
     * 所属字典类型标识，关联 sys_dict_type.dict_type
     */
    private String dictType;

    /**
     * 样式属性（其他样式扩展）
     */
    private String cssClass;

    /**
     * 表格回显样式，如 primary/success/danger
     */
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    private String isDefault;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}

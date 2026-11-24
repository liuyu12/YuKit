package cn.lycoder.boot.module.system.dal.dataobject.dict;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型 DO，对应表 sys_dict_type
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SystemDictTypeDO extends BaseEntity {

    /**
     * 字典状态 - 正常
     */
    public static final String STATUS_ENABLE = "0";
    /**
     * 字典状态 - 停用
     */
    public static final String STATUS_DISABLE = "1";

    /**
     * 字典主键
     */
    @TableId
    private Long dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型标识（唯一），如 sys_normal_disable，字典数据据此归类
     */
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}

package cn.lycoder.boot.module.system.dal.dataobject.config;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统参数 DO，对应表 sys_config
 * <p>
 * 键值对配置，运行时可改；configType='Y' 为内置参数，不允许删除（RuoYi 约定）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SystemConfigDO extends BaseEntity {

    /**
     * 参数类型 - 内置
     */
    public static final String TYPE_BUILT_IN = "Y";
    /**
     * 参数类型 - 自定义
     */
    public static final String TYPE_CUSTOM = "N";

    /**
     * 参数主键
     */
    @TableId
    private Long configId;

    /**
     * 参数名称
     */
    private String configName;

    /**
     * 参数键名（全局唯一）
     */
    private String configKey;

    /**
     * 参数键值
     */
    private String configValue;

    /**
     * 是否内置：Y 是 / N 否
     */
    private String configType;

    /**
     * 备注
     */
    private String remark;
}

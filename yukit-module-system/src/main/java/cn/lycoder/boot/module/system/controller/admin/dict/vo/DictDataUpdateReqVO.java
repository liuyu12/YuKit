package cn.lycoder.boot.module.system.controller.admin.dict.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典数据更新 Request VO
 * <p>
 * dictCode 由路径参数传入并覆盖，请求体可省略
 */
@Data
public class DictDataUpdateReqVO {

    /**
     * 字典编码（由路径参数传入并覆盖）
     */
    private Long dictCode;

    /**
     * 所属字典类型标识
     */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /**
     * 字典标签
     */
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    /**
     * 字典键值
     */
    @NotBlank(message = "字典键值不能为空")
    private String dictValue;

    /**
     * 字典排序
     */
    private Integer dictSort;

    /**
     * 样式属性
     */
    private String cssClass;

    /**
     * 表格回显样式
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

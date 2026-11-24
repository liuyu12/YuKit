package cn.lycoder.boot.module.system.controller.admin.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据 Response VO
 */
@Data
public class DictDataRespVO implements Serializable {

    /**
     * 字典编码
     */
    @Schema(description = "字典编码", example = "1")
    private Long dictCode;

    /**
     * 字典排序
     */
    @Schema(description = "字典排序", example = "1")
    private Integer dictSort;

    /**
     * 字典标签
     */
    @Schema(description = "字典标签", example = "正常")
    private String dictLabel;

    /**
     * 字典键值
     */
    @Schema(description = "字典键值", example = "0")
    private String dictValue;

    /**
     * 所属字典类型标识
     */
    @Schema(description = "所属字典类型标识", example = "sys_normal_disable")
    private String dictType;

    /**
     * 样式属性
     */
    @Schema(description = "样式属性")
    private String cssClass;

    /**
     * 表格回显样式
     */
    @Schema(description = "表格回显样式", example = "primary")
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    @Schema(description = "是否默认（Y是 N否）", example = "N")
    private String isDefault;

    /**
     * 状态（0正常 1停用）
     */
    @Schema(description = "状态（0正常 1停用）", example = "0")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "正常状态")
    private String remark;
}

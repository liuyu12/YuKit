package cn.lycoder.boot.module.system.controller.admin.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型 Response VO
 */
@Data
public class DictTypeRespVO implements Serializable {

    /**
     * 字典主键
     */
    @Schema(description = "字典主键", example = "1")
    private Long dictId;

    /**
     * 字典名称
     */
    @Schema(description = "字典名称", example = "系统开关")
    private String dictName;

    /**
     * 字典类型标识
     */
    @Schema(description = "字典类型标识", example = "sys_normal_disable")
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    @Schema(description = "状态（0正常 1停用）", example = "0")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "系统启用/停用开关字典")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private LocalDateTime createTime;
}

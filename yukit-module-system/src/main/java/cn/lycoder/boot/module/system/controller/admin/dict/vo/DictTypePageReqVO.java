package cn.lycoder.boot.module.system.controller.admin.dict.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictTypePageReqVO extends PageParam {

    /**
     * 字典名称，模糊匹配
     */
    @Schema(description = "字典名称，模糊匹配", example = "开关")
    private String dictName;

    /**
     * 字典类型标识，模糊匹配
     */
    @Schema(description = "字典类型标识，模糊匹配", example = "sys_normal")
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    @Schema(description = "状态（0正常 1停用）", example = "0")
    private String status;
}

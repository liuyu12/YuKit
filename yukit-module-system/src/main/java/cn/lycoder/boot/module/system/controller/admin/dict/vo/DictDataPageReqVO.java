package cn.lycoder.boot.module.system.controller.admin.dict.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataPageReqVO extends PageParam {

    /**
     * 所属字典类型标识
     */
    @Schema(description = "所属字典类型标识", example = "sys_normal_disable")
    private String dictType;

    /**
     * 字典标签，模糊匹配
     */
    @Schema(description = "字典标签，模糊匹配", example = "正常")
    private String dictLabel;

    /**
     * 状态（0正常 1停用）
     */
    @Schema(description = "状态（0正常 1停用）", example = "0")
    private String status;
}

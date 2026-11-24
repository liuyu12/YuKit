package cn.lycoder.boot.module.system.controller.admin.dict.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 字典类型更新 Request VO
 * <p>
 * dictId 由路径参数传入并覆盖，请求体可省略
 */
@Data
public class DictTypeUpdateReqVO {

    /**
     * 字典主键（由路径参数传入并覆盖）
     */
    private Long dictId;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    /**
     * 字典类型标识（唯一）
     */
    @NotBlank(message = "字典类型标识不能为空")
    @Pattern(regexp = "^\\w+$", message = "字典类型标识只能包含字母、数字和下划线")
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

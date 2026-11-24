package cn.lycoder.boot.module.system.controller.admin.dict.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 字典类型创建 Request VO
 */
@Data
public class DictTypeCreateReqVO {

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    /**
     * 字典类型标识（唯一），仅允许字母、数字、下划线
     */
    @NotBlank(message = "字典类型标识不能为空")
    @Pattern(regexp = "^\\w+$", message = "字典类型标识只能包含字母、数字和下划线")
    private String dictType;

    /**
     * 状态（0正常 1停用），为空默认正常
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}

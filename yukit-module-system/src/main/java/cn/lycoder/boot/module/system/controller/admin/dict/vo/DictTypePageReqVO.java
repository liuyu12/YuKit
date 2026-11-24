package cn.lycoder.boot.module.system.controller.admin.dict.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
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
    private String dictName;

    /**
     * 字典类型标识，模糊匹配
     */
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    private String status;
}

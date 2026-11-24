package cn.lycoder.boot.module.system.controller.admin.dict.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
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
    private String dictType;

    /**
     * 字典标签，模糊匹配
     */
    private String dictLabel;

    /**
     * 状态（0正常 1停用）
     */
    private String status;
}

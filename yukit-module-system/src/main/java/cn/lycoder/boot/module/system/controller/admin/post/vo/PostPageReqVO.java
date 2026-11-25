package cn.lycoder.boot.module.system.controller.admin.post.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 岗位分页查询 Request VO")
public class PostPageReqVO extends PageParam {

    /**
     * 岗位编码，模糊匹配
     */
    @Schema(description = "岗位编码，模糊匹配", example = "se")
    private String postCode;

    /**
     * 岗位名称，模糊匹配
     */
    @Schema(description = "岗位名称，模糊匹配", example = "经理")
    private String postName;

    /**
     * 状态：0 正常 / 1 停用，精确匹配
     */
    @Schema(description = "状态：0 正常 / 1 停用", example = "0")
    private String status;
}

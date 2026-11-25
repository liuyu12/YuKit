package cn.lycoder.boot.module.system.controller.admin.post.vo;

import cn.lycoder.boot.framework.common.validation.InDict;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 岗位创建 Request VO
 */
@Data
@Schema(description = "管理后台 - 岗位创建 Request VO")
public class PostCreateReqVO {

    @NotBlank(message = "岗位编码不能为空")
    @Size(max = 64, message = "岗位编码长度不能超过 64")
    @Schema(description = "岗位编码（全局唯一）", example = "dev")
    private String postCode;

    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 50, message = "岗位名称长度不能超过 50")
    @Schema(description = "岗位名称", example = "开发工程师")
    private String postName;

    @Size(max = 50, message = "岗位类别长度不能超过 50")
    @Schema(description = "岗位类别编码（预留分组维度）", example = "tech")
    private String postCategory;

    @PositiveOrZero(message = "显示顺序不能为负数")
    @Schema(description = "显示顺序", example = "10")
    private Integer postSort;

    @InDict("sys_normal_disable")
    @Schema(description = "状态：0 正常 / 1 停用，为空默认 0", example = "0")
    private String status;

    @Size(max = 500, message = "备注长度不能超过 500")
    @Schema(description = "备注", example = "研发序列")
    private String remark;
}

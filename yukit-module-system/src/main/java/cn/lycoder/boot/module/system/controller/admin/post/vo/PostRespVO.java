package cn.lycoder.boot.module.system.controller.admin.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位 Response VO
 * <p>
 * 实现 Serializable 以满足 PageResult.convert 的泛型上界约束。
 */
@Data
@Schema(description = "管理后台 - 岗位 Response VO")
public class PostRespVO implements Serializable {

    @Schema(description = "岗位ID", example = "1")
    private Long postId;

    @Schema(description = "岗位编码", example = "se")
    private String postCode;

    @Schema(description = "岗位名称", example = "项目经理")
    private String postName;

    @Schema(description = "岗位类别编码", example = "manage")
    private String postCategory;

    @Schema(description = "显示顺序", example = "2")
    private Integer postSort;

    @Schema(description = "状态：0 正常 / 1 停用", example = "0")
    private String status;

    @Schema(description = "备注", example = "一线管理岗")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

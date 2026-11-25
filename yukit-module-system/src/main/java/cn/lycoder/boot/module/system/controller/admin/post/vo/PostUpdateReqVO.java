package cn.lycoder.boot.module.system.controller.admin.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位更新 Request VO
 * <p>
 * 复用创建字段全集（对齐 JobUpdateReqVO extends JobCreateReqVO 先例）；
 * 编码允许修改，唯一性由 Service 层排除自身后校验。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 岗位更新 Request VO")
public class PostUpdateReqVO extends PostCreateReqVO {

    @NotNull(message = "岗位ID不能为空")
    @Schema(description = "岗位ID", example = "1")
    private Long postId;
}

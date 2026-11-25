package cn.lycoder.boot.module.system.controller.admin.file.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文件分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 文件分页查询 Request VO")
public class FilePageReqVO extends PageParam {

    @Schema(description = "原始文件名，模糊匹配", example = "截图")
    private String originalName;

    @Schema(description = "MIME 类型，模糊匹配", example = "image/")
    private String mimeType;

    @Schema(description = "创建时间范围-起")
    private LocalDateTime beginTime;

    @Schema(description = "创建时间范围-止")
    private LocalDateTime endTime;
}

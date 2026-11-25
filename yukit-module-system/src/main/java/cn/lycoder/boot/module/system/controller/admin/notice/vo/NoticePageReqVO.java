package cn.lycoder.boot.module.system.controller.admin.notice.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公告分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 公告分页查询 Request VO")
public class NoticePageReqVO extends PageParam {

    /**
     * 公告标题，模糊匹配
     */
    @Schema(description = "公告标题，模糊匹配", example = "维护")
    private String noticeTitle;

    /**
     * 公告类型：1 通知 / 2 公告
     */
    @Schema(description = "公告类型：1 通知 / 2 公告", example = "1")
    private String noticeType;

    /**
     * 状态：0 正常 / 1 关闭
     */
    @Schema(description = "状态：0 正常 / 1 关闭", example = "0")
    private String status;

    /**
     * 创建时间范围 - 起（含），命名对齐 LoginLog/JobLog 分页先例
     */
    @Schema(description = "创建时间起（含）", example = "2026-01-01 00:00:00")
    private LocalDateTime beginTime;

    /**
     * 创建时间范围 - 止（含）
     */
    @Schema(description = "创建时间止（含）", example = "2026-12-31 23:59:59")
    private LocalDateTime endTime;
}

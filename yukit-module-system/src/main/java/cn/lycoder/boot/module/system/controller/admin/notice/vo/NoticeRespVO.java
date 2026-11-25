package cn.lycoder.boot.module.system.controller.admin.notice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告 Response VO
 */
@Data
@Schema(description = "管理后台 - 公告 Response VO")
public class NoticeRespVO implements Serializable {

    /**
     * 公告主键
     */
    @Schema(description = "公告主键", example = "1")
    private Long noticeId;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题", example = "系统维护通知")
    private String noticeTitle;

    /**
     * 公告类型：1 通知 / 2 公告
     */
    @Schema(description = "公告类型：1 通知 / 2 公告", example = "1")
    private String noticeType;

    /**
     * 公告内容
     */
    @Schema(description = "公告内容", example = "本周六凌晨停机升级")
    private String noticeContent;

    /**
     * 状态：0 正常 / 1 关闭
     */
    @Schema(description = "状态：0 正常 / 1 关闭", example = "0")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "停机维护")
    private String remark;

    /**
     * 创建者
     */
    @Schema(description = "创建者", example = "admin")
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-01-02 12:00:00")
    private LocalDateTime updateTime;
}

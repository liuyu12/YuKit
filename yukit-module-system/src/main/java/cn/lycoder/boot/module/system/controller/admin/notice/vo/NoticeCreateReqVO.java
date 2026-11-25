package cn.lycoder.boot.module.system.controller.admin.notice.vo;

import cn.lycoder.boot.framework.common.validation.InDict;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 公告创建 Request VO
 */
@Data
@Schema(description = "管理后台 - 公告创建 Request VO")
public class NoticeCreateReqVO {

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 50, message = "公告标题长度不能超过 50")
    @Schema(description = "公告标题", example = "系统维护通知")
    private String noticeTitle;

    @NotBlank(message = "公告类型不能为空")
    @InDict("sys_notice_type")
    @Schema(description = "公告类型：1 通知 / 2 公告（字典 sys_notice_type）", example = "1")
    private String noticeType;

    @Size(max = 60000, message = "公告内容过长")
    @Schema(description = "公告内容（纯文本或富文本 HTML）", example = "本周六凌晨停机升级")
    private String noticeContent;

    @InDict("sys_notice_status")
    @Schema(description = "状态：0 正常 / 1 关闭，为空默认 0", example = "0")
    private String status;

    @Size(max = 255, message = "备注长度不能超过 255")
    @Schema(description = "备注", example = "停机维护")
    private String remark;
}

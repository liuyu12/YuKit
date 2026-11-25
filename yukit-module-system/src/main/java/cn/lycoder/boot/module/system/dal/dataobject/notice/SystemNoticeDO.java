package cn.lycoder.boot.module.system.dal.dataobject.notice;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告 DO，对应表 sys_notice
 * <p>
 * noticeType/status 取值由字典 sys_notice_type、sys_notice_status 约束（@InDict 校验）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SystemNoticeDO extends BaseEntity {

    /**
     * 状态 - 正常（前台可见）
     */
    public static final String STATUS_NORMAL = "0";
    /**
     * 状态 - 关闭（前台不再展示）
     */
    public static final String STATUS_CLOSED = "1";

    /**
     * 公告主键
     */
    @TableId
    private Long noticeId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型：1 通知 / 2 公告（字典 sys_notice_type）
     */
    private String noticeType;

    /**
     * 公告内容（纯文本或富文本 HTML，渲染防 XSS 由前端负责）
     */
    private String noticeContent;

    /**
     * 状态：0 正常 / 1 关闭（字典 sys_notice_status）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}

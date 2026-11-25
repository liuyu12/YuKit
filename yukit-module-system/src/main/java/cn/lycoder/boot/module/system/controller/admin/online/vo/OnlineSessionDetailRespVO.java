package cn.lycoder.boot.module.system.controller.admin.online.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 在线会话详情 Response VO
 * <p>
 * 在列表字段基础上补充账号维度信息：设备类型、该账号登录设备数、
 * 会话剩余存活时间（区分"绝对过期"与"暂不超时"两种语义）。
 */
@Schema(description = "在线会话详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OnlineSessionDetailRespVO extends OnlineSessionRespVO {

    @Schema(description = "登录设备类型（PC/APP 等）")
    private String deviceType;

    @Schema(description = "该账号当前登录设备数")
    private Integer totalDeviceCount;

    @Schema(description = "账号会话创建时间")
    private LocalDateTime sessionCreateTime;

    @Schema(description = "会话剩余存活秒数；-2 表示永不过期（SaTokenDao.NOT_VALUE_EXPIRE）")
    private Long sessionTimeToLive;
}

package cn.lycoder.boot.module.system.service.online;

import cn.lycoder.boot.module.system.controller.admin.online.vo.OnlineSessionDetailRespVO;
import cn.lycoder.boot.module.system.controller.admin.online.vo.OnlineSessionRespVO;

import java.util.List;

/**
 * 在线会话管理 Service
 * <p>
 * 基于 Sa-Token 会话：登录时由 {@code AuthServiceImpl} 将 IP/UA/登录时间写入 token session，
 * 本服务负责枚举在线令牌、组装展示信息并提供强制下线能力。
 */
public interface OnlineSessionService {

    /** token session 中登录 IP 的属性键 */
    String ATTR_LOGIN_IP = "online:loginIp";
    /** token session 中浏览器 UA 的属性键 */
    String ATTR_USER_AGENT = "online:userAgent";
    /** token session 中登录时间（epoch 毫秒）的属性键 */
    String ATTR_LOGIN_TIME = "online:loginTime";

    /**
     * 查询当前全部在线会话（按登录时间倒序）
     */
    List<OnlineSessionRespVO> getOnlineSessionList();

    /**
     * 查询单个在线会话详情（列表字段 + 设备类型/登录设备数/会话剩余存活等账号维度信息）
     *
     * @param tokenValue 目标会话令牌
     * @return 会话详情；令牌已失效抛 ONLINE_SESSION_NOT_EXISTS
     */
    OnlineSessionDetailRespVO getOnlineSession(String tokenValue);

    /**
     * 强制指定会话下线（踢下线，令牌再访问将提示"被踢下线"）
     *
     * @param tokenValue 目标会话令牌
     */
    void forceLogout(String tokenValue);

    /**
     * 强制某用户全部设备下线
     */
    void forceLogoutByUserId(Long userId);
}

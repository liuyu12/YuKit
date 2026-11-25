package cn.lycoder.boot.module.system.service.online;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.mybatis.datapermission.core.DataPermissionUtils;
import cn.lycoder.boot.module.system.controller.admin.online.vo.OnlineSessionDetailRespVO;
import cn.lycoder.boot.module.system.controller.admin.online.vo.OnlineSessionRespVO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.ONLINE_SESSION_NOT_EXISTS;

/**
 * 在线会话管理 Service 实现
 * <p>
 * 通过 {@link StpUtil#searchTokenValue} 枚举全部令牌，逐个解析有效登录态并读取 token session 元数据；
 * 用户名/昵称按在线用户 ID 批量反查，此查询跨越当前用户可见部门，故 {@code executeIgnore} 关闭数据权限。
 */
@Service
@RequiredArgsConstructor
public class OnlineSessionServiceImpl implements OnlineSessionService {

    private final SystemUserMapper userMapper;

    /**
     * 扫描全部令牌时使用的关键字：Sa-Token 的 {@code searchTokenValue} 语义是 glob 匹配，
     * 传空串只匹配空前缀（等价于什么都找不到），必须用 "*" 才能全量匹配。
     */
    private static final String TOKEN_SEARCH_PATTERN = "*";
    /** 单次扫描上界，避免 Redis 上堆积过多令牌时打爆内存。 */
    private static final int TOKEN_SCAN_LIMIT = 1000;

    @Override
    public List<OnlineSessionRespVO> getOnlineSessionList() {
        List<String> tokens = StpUtil.searchTokenValue(TOKEN_SEARCH_PATTERN, 0, TOKEN_SCAN_LIMIT, false);
        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }
        // 1. 令牌 → 在线会话（过滤已失效/已登出的令牌）
        //    Sa-Token 的 searchTokenValue 返回的是完整 key（形如 satoken:login:token:xxx），
        //    而 getLoginIdByToken 入参需要的是纯 tokenValue，所以先取最后一段。
        List<OnlineSessionRespVO> sessions = tokens.stream()
                .map(OnlineSessionServiceImpl::stripKeyPrefix)
                .map(this::resolveSession)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 2. 批量补齐用户信息（忽略数据权限，管理员需看全量在线用户归属）
        fillUserInfo(sessions);
        // 3. 按登录时间倒序（新的在前）
        return sessions.stream()
                .sorted(Comparator.comparing(OnlineSessionRespVO::getLoginTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * 去掉 Sa-Token 存储型 key 前缀：形如 {@code satoken:login:token:xxx} 取最后一段 {@code xxx}；
     * 若传入已经是纯 tokenValue（不含冒号），原样返回。
     */
    private static String stripKeyPrefix(String tokenOrKey) {
        if (tokenOrKey == null) {
            return null;
        }
        int idx = tokenOrKey.lastIndexOf(':');
        return idx < 0 ? tokenOrKey : tokenOrKey.substring(idx + 1);
    }

    /**
     * 解析单个令牌为在线会话；令牌已失效（无对应登录态）则返回 null
     */
    private OnlineSessionRespVO resolveSession(String token) {
        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId == null) {
            return null;
        }
        OnlineSessionRespVO vo = new OnlineSessionRespVO();
        vo.setTokenValue(token);
        vo.setUserId(Long.valueOf(loginId.toString()));
        SaSession session = StpUtil.getTokenSessionByToken(token);
        if (session != null) {
            vo.setLoginIp(asString(session.get(ATTR_LOGIN_IP)));
            vo.setUserAgent(asString(session.get(ATTR_USER_AGENT)));
            vo.setLoginTime(toDateTime(session.get(ATTR_LOGIN_TIME)));
        }
        return vo;
    }

    /**
     * 按在线用户 ID 批量反查账号与昵称，命中的回填到各会话
     */
    private void fillUserInfo(List<OnlineSessionRespVO> sessions) {
        Set<Long> userIds = sessions.stream()
                .map(OnlineSessionRespVO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, SystemUserDO> userMap = DataPermissionUtils.executeIgnore(() ->
                userMapper.selectByIds(userIds).stream()
                        .collect(Collectors.toMap(SystemUserDO::getUserId, Function.identity(), (a, b) -> a)));
        sessions.forEach(vo -> {
            SystemUserDO user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setUsername(user.getUserName());
                vo.setNickname(user.getNickName());
            }
        });
    }

    @Override
    public OnlineSessionDetailRespVO getOnlineSession(String tokenValue) {
        // 令牌失效直接拒绝，与 forceLogout 同语义（详情页是强操作的入口，不对死会话降级展示）
        Object loginId = StpUtil.getLoginIdByToken(tokenValue);
        if (loginId == null) {
            throw new ServiceException(ONLINE_SESSION_NOT_EXISTS);
        }
        OnlineSessionRespVO base = resolveSession(tokenValue);
        if (base == null) {
            throw new ServiceException(ONLINE_SESSION_NOT_EXISTS);
        }
        fillUserInfo(List.of(base));
        OnlineSessionDetailRespVO detail = new OnlineSessionDetailRespVO();
        BeanUtils.copyProperties(base, detail);
        // 账号维度信息：从 Account-Session 终端列表定位本令牌，顺带取设备类型/登录设备数
        SaSession accountSession = DataPermissionUtils.executeIgnore(
                () -> StpUtil.getSessionByLoginId(loginId, false));
        if (accountSession != null) {
            List<SaTerminalInfo> terminals = accountSession.terminalListCopy();
            detail.setTotalDeviceCount(terminals.size());
            terminals.stream()
                    .filter(t -> Objects.equals(t.getTokenValue(), tokenValue))
                    .findFirst()
                    .ifPresent(t -> detail.setDeviceType(t.getDeviceType()));
            detail.setSessionCreateTime(toDateTime(accountSession.getCreateTime()));
            detail.setSessionTimeToLive(getSessionTimeToLive(accountSession.getId()));
        }
        return detail;
    }

    /**
     * 账号会话剩余存活秒数（SaTokenDao.NOT_VALUE_EXPIRE=-2 表示永不过期）：
     * SaSession 不提供绝对过期时间，只能绕 dao 查 TTL；
     * key 拼装须与 Sa-Token 存储型 key 一致：{tokenName}:{loginType}:session:{sessionId}
     */
    private static Long getSessionTimeToLive(String sessionId) {
        String key = SaManager.getConfig().getTokenName() + ":" + StpUtil.getLoginType() + ":session:" + sessionId;
        return SaManager.getSaTokenDao().getSessionTimeout(key);
    }

    @Override
    public void forceLogout(String tokenValue) {
        // 令牌无对应登录态视为已下线，幂等返回错误提示
        if (StpUtil.getLoginIdByToken(tokenValue) == null) {
            throw new ServiceException(ONLINE_SESSION_NOT_EXISTS);
        }
        StpUtil.kickoutByTokenValue(tokenValue);
    }

    @Override
    public void forceLogoutByUserId(Long userId) {
        StpUtil.kickout(userId);
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static LocalDateTime toDateTime(Object epochMillis) {
        if (epochMillis instanceof Number number) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(number.longValue()), ZoneId.systemDefault());
        }
        return null;
    }
}

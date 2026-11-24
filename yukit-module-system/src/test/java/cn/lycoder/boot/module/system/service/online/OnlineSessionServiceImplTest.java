package cn.lycoder.boot.module.system.service.online;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.online.vo.OnlineSessionRespVO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link OnlineSessionServiceImpl} 在线会话枚举与强制下线单元测试（mockStatic StpUtil）
 */
class OnlineSessionServiceImplTest {

    private SystemUserMapper userMapper;
    private OnlineSessionServiceImpl service;

    @BeforeEach
    void setUp() {
        userMapper = mock(SystemUserMapper.class);
        service = new OnlineSessionServiceImpl(userMapper);
    }

    private SystemUserDO user(Long id, String name, String nick) {
        SystemUserDO u = new SystemUserDO();
        u.setUserId(id);
        u.setUserName(name);
        u.setNickName(nick);
        return u;
    }

    @Test
    @DisplayName("列表：过滤失效令牌、补齐用户信息、解析会话元数据")
    void listFiltersAndEnriches() {
        SaSession session = mock(SaSession.class);
        when(session.get(OnlineSessionService.ATTR_LOGIN_IP)).thenReturn("1.1.1.1");
        when(session.get(OnlineSessionService.ATTR_USER_AGENT)).thenReturn("curl/8");
        when(session.get(OnlineSessionService.ATTR_LOGIN_TIME)).thenReturn(1_700_000_000_000L);
        when(userMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(user(1L, "admin", "超级管理员")));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.searchTokenValue("*", 0, 1000, false))
                    .thenReturn(List.of("t1", "t2"));
            stp.when(() -> StpUtil.getLoginIdByToken("t1")).thenReturn(1L);
            stp.when(() -> StpUtil.getLoginIdByToken("t2")).thenReturn(null); // 已失效
            stp.when(() -> StpUtil.getTokenSessionByToken("t1")).thenReturn(session);

            List<OnlineSessionRespVO> result = service.getOnlineSessionList();

            assertThat(result).hasSize(1);
            OnlineSessionRespVO vo = result.get(0);
            assertThat(vo.getTokenValue()).isEqualTo("t1");
            assertThat(vo.getUserId()).isEqualTo(1L);
            assertThat(vo.getUsername()).isEqualTo("admin");
            assertThat(vo.getNickname()).isEqualTo("超级管理员");
            assertThat(vo.getLoginIp()).isEqualTo("1.1.1.1");
            assertThat(vo.getLoginTime()).isNotNull();
        }
    }

    @Test
    @DisplayName("无令牌：直接返回空列表，不查库")
    void emptyTokens() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.searchTokenValue("*", 0, 1000, false)).thenReturn(List.of());
            assertThat(service.getOnlineSessionList()).isEmpty();
        }
        verify(userMapper, never()).selectByIds(anyCollection());
    }

    @Test
    @DisplayName("强制下线：令牌有效则踢下线")
    void forceLogoutValid() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.getLoginIdByToken("t1")).thenReturn(1L);
            service.forceLogout("t1");
            stp.verify(() -> StpUtil.kickoutByTokenValue("t1"));
        }
    }

    @Test
    @DisplayName("强制下线：令牌已失效则抛异常，不触发踢人")
    void forceLogoutInvalid() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.getLoginIdByToken("ghost")).thenReturn(null);
            assertThatThrownBy(() -> service.forceLogout("ghost"))
                    .isInstanceOf(ServiceException.class);
            stp.verify(() -> StpUtil.kickoutByTokenValue(any()), never());
        }
    }
}

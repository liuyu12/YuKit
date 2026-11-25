package cn.lycoder.boot.module.system.service.online;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.online.vo.OnlineSessionDetailRespVO;
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
    @DisplayName("详情：令牌有效时叠加设备类型/登录设备数/会话存活等账号维度信息")
    void detailEnrichesAccountDimension() {
        SaTokenDao dao = mock(SaTokenDao.class);
        when(dao.getSessionTimeout("satoken:login:session:account-r-1")).thenReturn(7200L);
        SaTerminalInfo terminal = new SaTerminalInfo(1, "t1", "PC", null);
        SaSession accountSession = mock(SaSession.class);
        when(accountSession.getId()).thenReturn("account-r-1");
        when(accountSession.getCreateTime()).thenReturn(1_700_000_000_000L);
        when(accountSession.terminalListCopy()).thenReturn(List.of(terminal));
        when(userMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(user(1L, "admin", "超级管理员")));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class);
             MockedStatic<SaManager> manager = mockStatic(SaManager.class)) {
            stp.when(() -> StpUtil.getLoginIdByToken("t1")).thenReturn(1L);
            stp.when(() -> StpUtil.getTokenSessionByToken("t1")).thenReturn(null);
            stp.when(() -> StpUtil.getSessionByLoginId(1L, false)).thenReturn(accountSession);
            stp.when(StpUtil::getLoginType).thenReturn("login");
            manager.when(SaManager::getConfig).thenReturn(new SaTokenConfig().setTokenName("satoken"));
            manager.when(SaManager::getSaTokenDao).thenReturn(dao);

            OnlineSessionDetailRespVO detail = service.getOnlineSession("t1");

            assertThat(detail.getTokenValue()).isEqualTo("t1");
            assertThat(detail.getUsername()).isEqualTo("admin");
            assertThat(detail.getDeviceType()).isEqualTo("PC");
            assertThat(detail.getTotalDeviceCount()).isEqualTo(1);
            assertThat(detail.getSessionCreateTime()).isNotNull();
            assertThat(detail.getSessionTimeToLive()).isEqualTo(7200L);
        }
    }

    @Test
    @DisplayName("详情：令牌已失效抛 ONLINE_SESSION_NOT_EXISTS，不查库")
    void detailRejectsDeadToken() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.getLoginIdByToken("ghost")).thenReturn(null);
            assertThatThrownBy(() -> service.getOnlineSession("ghost"))
                    .isInstanceOf(ServiceException.class);
        }
        verify(userMapper, never()).selectByIds(anyCollection());
    }

    @Test
    @DisplayName("详情：账号会话不存在时仅返回列表字段，不报错")
    void detailToleratesMissingAccountSession() {
        when(userMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(user(2L, "guest", "访客")));

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.getLoginIdByToken("t9")).thenReturn(2L);
            stp.when(() -> StpUtil.getTokenSessionByToken("t9")).thenReturn(null);
            stp.when(() -> StpUtil.getSessionByLoginId(2L, false)).thenReturn(null);

            OnlineSessionDetailRespVO detail = service.getOnlineSession("t9");

            assertThat(detail.getUsername()).isEqualTo("guest");
            assertThat(detail.getDeviceType()).isNull();
            assertThat(detail.getSessionTimeToLive()).isNull();
        }
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

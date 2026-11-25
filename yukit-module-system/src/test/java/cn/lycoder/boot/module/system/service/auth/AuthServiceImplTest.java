package cn.lycoder.boot.module.system.service.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import cn.lycoder.boot.module.system.enums.logger.LoginResultTypeEnum;
import cn.lycoder.boot.module.system.service.captcha.CaptchaService;
import cn.lycoder.boot.module.system.service.logger.LoginLogService;
import cn.lycoder.boot.module.system.service.online.OnlineSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.ZoneId;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_LOGIN_ACCOUNT_LOCKED;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_LOGIN_BAD_CREDENTIALS;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_LOGIN_USER_DISABLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthServiceImpl} 登录主流程单元测试：锁定拦截、密码错误计数、禁用、成功解锁与日志埋点
 */
class AuthServiceImplTest {

    /**
     * 与全局 Clock Bean 同构的测试时钟（yukit.zone-id 默认值）
     */
    private static final Clock TEST_CLOCK = Clock.system(ZoneId.of("Asia/Shanghai"));

    private SystemUserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private CaptchaService captchaService;
    private LoginAttemptService loginAttemptService;
    private LoginLogService loginLogService;
    private AuthServiceImpl authService;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        userMapper = mock(SystemUserMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        captchaService = mock(CaptchaService.class);
        loginAttemptService = mock(LoginAttemptService.class);
        loginLogService = mock(LoginLogService.class);
        authService = new AuthServiceImpl(userMapper, passwordEncoder, captchaService,
                loginAttemptService, loginLogService, TEST_CLOCK);
        request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
    }

    private AuthLoginReqVO req(String username, String password) {
        AuthLoginReqVO reqVO = new AuthLoginReqVO();
        reqVO.setUsername(username);
        reqVO.setPassword(password);
        return reqVO;
    }

    private SystemUserDO user(Long id, String name, String status) {
        SystemUserDO u = new SystemUserDO();
        u.setUserId(id);
        u.setUserName(name);
        u.setStatus(status);
        u.setPassword("encoded");
        return u;
    }

    @Test
    @DisplayName("锁定期内：直接拒绝，不查库、不计数，写 ACCOUNT_LOCKED 日志")
    void locked() {
        when(loginAttemptService.isLocked("ry")).thenReturn(true);
        when(loginAttemptService.getLockRemainMinutes("ry")).thenReturn(10L);

        assertThatThrownBy(() -> authService.login(req("ry", "x"), request))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getCode())
                        .isEqualTo(AUTH_LOGIN_ACCOUNT_LOCKED.getCode()))
                .hasMessageContaining("10");

        verify(userMapper, never()).selectOne(any());
        verify(loginAttemptService, never()).recordFailure(any());
        verify(loginLogService).createLoginLog(isNull(), eq("ry"),
                eq(LoginResultTypeEnum.ACCOUNT_LOCKED), any(), any(), any());
    }

    @Test
    @DisplayName("账号不存在：累加失败计数，写 BAD_CREDENTIALS 日志，账号枚举同码")
    void badCredentialsUserNull() {
        when(loginAttemptService.isLocked("ghost")).thenReturn(false);
        when(userMapper.selectOne(any())).thenReturn(null);
        when(loginAttemptService.recordFailure("ghost")).thenReturn(3);

        assertThatThrownBy(() -> authService.login(req("ghost", "x"), request))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getCode())
                        .isEqualTo(AUTH_LOGIN_BAD_CREDENTIALS.getCode()));

        verify(loginAttemptService).recordFailure("ghost");
        verify(loginLogService).createLoginLog(isNull(), eq("ghost"),
                eq(LoginResultTypeEnum.BAD_CREDENTIALS), any(), any(), any());
    }

    @Test
    @DisplayName("密码错误：不匹配则拒绝并计数")
    void badCredentialsWrongPassword() {
        SystemUserDO u = user(2L, "ry", SystemUserDO.STATUS_ENABLE);
        when(loginAttemptService.isLocked("ry")).thenReturn(false);
        when(userMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req("ry", "wrong"), request))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getCode())
                        .isEqualTo(AUTH_LOGIN_BAD_CREDENTIALS.getCode()));

        verify(loginLogService).createLoginLog(eq(2L), eq("ry"),
                eq(LoginResultTypeEnum.BAD_CREDENTIALS), any(), any(), any());
    }

    @Test
    @DisplayName("密码正确但账号禁用：写 USER_DISABLED 日志，不签发令牌")
    void disabled() {
        SystemUserDO u = user(2L, "ry", SystemUserDO.STATUS_DISABLE);
        when(loginAttemptService.isLocked("ry")).thenReturn(false);
        when(userMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.matches("admin123", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(req("ry", "admin123"), request))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getCode())
                        .isEqualTo(AUTH_LOGIN_USER_DISABLED.getCode()));

        verify(loginLogService).createLoginLog(eq(2L), eq("ry"),
                eq(LoginResultTypeEnum.USER_DISABLED), any(), any(), any());
    }

    @Test
    @DisplayName("登录成功：解锁、签发令牌、更新登录信息、写 SUCCESS 日志")
    void success() {
        SystemUserDO u = user(2L, "ry", SystemUserDO.STATUS_ENABLE);
        when(loginAttemptService.isLocked("ry")).thenReturn(false);
        when(userMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.matches("admin123", "encoded")).thenReturn(true);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            SaSession session = mock(SaSession.class);
            when(session.set(anyString(), any())).thenReturn(session);
            stp.when(StpUtil::getTokenSession).thenReturn(session);
            stp.when(() -> StpUtil.getTokenName()).thenReturn("satoken");
            stp.when(() -> StpUtil.getTokenValue()).thenReturn("token-abc");

            AuthLoginRespVO resp = authService.login(req("ry", "admin123"), request);

            assertThat(resp.getUserId()).isEqualTo(2L);
            assertThat(resp.getTokenValue()).isEqualTo("token-abc");
            stp.verify(() -> StpUtil.login(2L));
            // 会话元数据已写入 token session
            verify(session).set(eq(OnlineSessionService.ATTR_LOGIN_IP), any());
        }

        verify(loginAttemptService).reset("ry");
        verify(userMapper).updateById(any(SystemUserDO.class));
        verify(loginLogService).createLoginLog(eq(2L), eq("ry"),
                eq(LoginResultTypeEnum.SUCCESS), any(), any(), any());
    }
}

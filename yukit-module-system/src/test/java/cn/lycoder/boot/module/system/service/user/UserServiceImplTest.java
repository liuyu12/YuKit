package cn.lycoder.boot.module.system.service.user;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.convert.user.UserConvertImpl;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants;
import cn.lycoder.boot.module.system.service.auth.LoginAttemptService;
import cn.lycoder.boot.module.system.service.config.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 单测：纯 Mockito，不起 Spring 上下文
 * <p>
 * 聚焦重置密码的取值优先级（显式密码 > sys.user.initPassword 参数 > 内置兜底）
 * 与连带解锁行为。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String CONFIG_KEY = "sys.user.initPassword";

    @Mock
    private SystemUserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private ConfigService configService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, new UserConvertImpl(),
                passwordEncoder, loginAttemptService, configService);
    }

    @Test
    void resetPasswordShouldUseExplicitPassword() {
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L, "ry"));
        when(passwordEncoder.encode("newPass123")).thenReturn("$2a$explicit");

        userService.resetPassword(2L, "newPass123");

        ArgumentCaptor<SystemUserDO> captor = ArgumentCaptor.forClass(SystemUserDO.class);
        verify(userMapper).updateById(captor.capture());
        SystemUserDO updated = captor.getValue();
        assertThat(updated.getUserId()).isEqualTo(2L);
        assertThat(updated.getPassword()).isEqualTo("$2a$explicit");
        // 只带密码列，其余字段 null 由 updateById 跳过
        assertThat(updated.getUserName()).isNull();
        // 显式指定时不应触达参数
        verify(configService, never()).getConfigValue(CONFIG_KEY);
        // 凭证变更后顺带解锁
        verify(loginAttemptService).reset("ry");
    }

    @Test
    void resetPasswordShouldFallToConfigParamWhenBlank() {
        when(userMapper.selectById(3L)).thenReturn(buildUser(3L, "tom"));
        when(configService.getConfigValue(CONFIG_KEY)).thenReturn("paramPwd888");
        when(passwordEncoder.encode("paramPwd888")).thenReturn("$2a$param");

        userService.resetPassword(3L, " ");

        ArgumentCaptor<SystemUserDO> captor = ArgumentCaptor.forClass(SystemUserDO.class);
        verify(userMapper).updateById(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$param");
    }

    @Test
    void resetPasswordShouldUseBuiltinDefaultWhenParamMissing() {
        when(userMapper.selectById(4L)).thenReturn(buildUser(4L, "jerry"));
        when(configService.getConfigValue(CONFIG_KEY)).thenReturn(null);
        when(passwordEncoder.encode("123456")).thenReturn("$2a$default");

        userService.resetPassword(4L, null);

        ArgumentCaptor<SystemUserDO> captor = ArgumentCaptor.forClass(SystemUserDO.class);
        verify(userMapper).updateById(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$default");
    }

    @Test
    void resetPasswordShouldRejectNotExists() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> userService.resetPassword(99L, "whatever"))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.USER_NOT_EXISTS.getCode());
        verify(userMapper, never()).updateById(any(SystemUserDO.class));
        verify(loginAttemptService, never()).reset(anyString());
    }

    @Test
    void unlockUserShouldResetAttemptOnly() {
        when(userMapper.selectById(5L)).thenReturn(buildUser(5L, "mike"));

        userService.unlockUser(5L);

        verify(loginAttemptService).reset("mike");
        verify(userMapper, never()).updateById(any(SystemUserDO.class));
    }

    private SystemUserDO buildUser(Long userId, String userName) {
        SystemUserDO user = new SystemUserDO();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setPassword("$2a$old");
        return user;
    }
}

package cn.lycoder.boot.module.system.service.user;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.UserProfileUpdateReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserUpdateReqVO;
import cn.lycoder.boot.module.system.convert.user.UserConvertImpl;
import cn.lycoder.boot.module.system.dal.dataobject.post.SystemPostDO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.post.SystemPostMapper;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import cn.lycoder.boot.module.system.dal.mapper.userpost.SystemUserPostMapper;
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

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private SystemUserPostMapper userPostMapper;
    @Mock
    private SystemPostMapper postMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, new UserConvertImpl(),
                passwordEncoder, loginAttemptService, configService, userPostMapper, postMapper);
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

    @Test
    void updateUserProfileShouldOnlyTouchSelfFields() {
        when(userMapper.selectById(6L)).thenReturn(buildUser(6L, "self"));
        UserProfileUpdateReqVO reqVO = new UserProfileUpdateReqVO();
        reqVO.setNickName("新昵称");
        reqVO.setEmail("me@example.com");
        reqVO.setPhonenumber("13800000000");
        reqVO.setSex("1");

        userService.updateUserProfile(6L, reqVO);

        ArgumentCaptor<SystemUserDO> captor = ArgumentCaptor.forClass(SystemUserDO.class);
        verify(userMapper).updateById(captor.capture());
        SystemUserDO updated = captor.getValue();
        assertThat(updated.getUserId()).isEqualTo(6L);
        assertThat(updated.getNickName()).isEqualTo("新昵称");
        assertThat(updated.getEmail()).isEqualTo("me@example.com");
        assertThat(updated.getSex()).isEqualTo("1");
        // 管理字段不得被自助接口触碰
        assertThat(updated.getUserName()).isNull();
        assertThat(updated.getPassword()).isNull();
        assertThat(updated.getStatus()).isNull();
        assertThat(updated.getDeptId()).isNull();
    }

    @Test
    void updateUserProfileShouldRejectNotExists() {
        when(userMapper.selectById(98L)).thenReturn(null);

        assertThatThrownBy(() -> userService.updateUserProfile(98L, new UserProfileUpdateReqVO()))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.USER_NOT_EXISTS.getCode());
        verify(userMapper, never()).updateById(any(SystemUserDO.class));
    }

    @Test
    void getUserProfileShouldReturnWhenExists() {
        when(userMapper.selectById(7L)).thenReturn(buildUser(7L, "viewer"));

        assertThat(userService.getUserProfile(7L).getUserName()).isEqualTo("viewer");
    }

    @Test
    void updateUserAvatarShouldOnlyTouchAvatarColumn() {
        when(userMapper.selectById(8L)).thenReturn(buildUser(8L, "pic"));

        userService.updateUserAvatar(8L, "/system/files/9/preview");

        ArgumentCaptor<SystemUserDO> captor = ArgumentCaptor.forClass(SystemUserDO.class);
        verify(userMapper).updateById(captor.capture());
        SystemUserDO updated = captor.getValue();
        assertThat(updated.getUserId()).isEqualTo(8L);
        assertThat(updated.getAvatar()).isEqualTo("/system/files/9/preview");
        // 仅头像列，其余字段不得被触碰
        assertThat(updated.getNickName()).isNull();
        assertThat(updated.getPassword()).isNull();
    }

    @Test
    void updateUserAvatarShouldRejectNotExists() {
        when(userMapper.selectById(97L)).thenReturn(null);

        assertThatThrownBy(() -> userService.updateUserAvatar(97L, "/system/files/1/preview"))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.USER_NOT_EXISTS.getCode());
        verify(userMapper, never()).updateById(any(SystemUserDO.class));
    }

    private SystemUserDO buildUser(Long userId, String userName) {
        SystemUserDO user = new SystemUserDO();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setPassword("$2a$old");
        return user;
    }

    @Test
    void createUserShouldSavePostLinksWhenPostsExist() {
        UserCreateReqVO reqVO = new UserCreateReqVO();
        reqVO.setUserName("newbie");
        reqVO.setPassword("pass123");
        reqVO.setPostIds(Arrays.asList(1L, 2L));
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(postMapper.selectBatchIds(any())).thenReturn(Arrays.asList(new SystemPostDO(), new SystemPostDO()));
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$new");
        when(userMapper.insert(any(SystemUserDO.class))).thenAnswer(inv -> {
            inv.getArgument(0, SystemUserDO.class).setUserId(10L);
            return 1;
        });

        Long userId = userService.createUser(reqVO);

        assertThat(userId).isEqualTo(10L);
        verify(userPostMapper).deleteByUserId(10L);
        verify(userPostMapper).insertBatch(10L, Arrays.asList(1L, 2L));
    }

    @Test
    void createUserShouldRejectWhenPostIdNotExists() {
        UserCreateReqVO reqVO = new UserCreateReqVO();
        reqVO.setUserName("newbie");
        reqVO.setPassword("pass123");
        reqVO.setPostIds(Arrays.asList(1L, 99L));
        when(userMapper.selectCount(any())).thenReturn(0L);
        // 仅 1 个岗位命中，小于去重后的 2 个 → 视为脏 ID
        when(postMapper.selectBatchIds(any())).thenReturn(Arrays.asList(new SystemPostDO()));

        assertThatThrownBy(() -> userService.createUser(reqVO))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.POST_NOT_EXISTS.getCode());
        verify(userMapper, never()).insert(any(SystemUserDO.class));
        verify(userPostMapper, never()).insertBatch(any(), any());
    }

    @Test
    void createUserShouldNotTouchPostLinkWhenNoPosts() {
        UserCreateReqVO reqVO = new UserCreateReqVO();
        reqVO.setUserName("solo");
        reqVO.setPassword("pass123");
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$s");
        when(userMapper.insert(any(SystemUserDO.class))).thenAnswer(inv -> {
            inv.getArgument(0, SystemUserDO.class).setUserId(11L);
            return 1;
        });

        userService.createUser(reqVO);

        // postIds 为空：validatePostsExist 短路不查岗位，saveUserPosts 只清不插
        verify(postMapper, never()).selectBatchIds(any());
        verify(userPostMapper).deleteByUserId(11L);
        verify(userPostMapper, never()).insertBatch(any(), any());
    }

    @Test
    void updateUserShouldOverwritePostLinks() {
        UserUpdateReqVO reqVO = new UserUpdateReqVO();
        reqVO.setUserId(30L);
        reqVO.setPostIds(Arrays.asList(3L));
        when(userMapper.selectById(30L)).thenReturn(buildUser(30L, "editor"));
        when(postMapper.selectBatchIds(any())).thenReturn(Arrays.asList(new SystemPostDO()));

        userService.updateUser(reqVO);

        verify(userMapper).updateById(any(SystemUserDO.class));
        verify(userPostMapper).deleteByUserId(30L);
        verify(userPostMapper).insertBatch(30L, Arrays.asList(3L));
    }

    @Test
    void deleteUserShouldPhysicallyClearPostLinks() {
        when(userMapper.selectById(20L)).thenReturn(buildUser(20L, "ghost"));

        userService.deleteUser(20L);

        verify(userMapper).deleteById(20L);
        verify(userPostMapper).deleteByUserId(20L);
    }
}

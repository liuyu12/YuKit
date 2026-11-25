package cn.lycoder.boot.module.system.service.auth;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.mybatis.datapermission.annotation.IgnoreDataPermission;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthUpdatePasswordReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import cn.lycoder.boot.module.system.enums.logger.LoginResultTypeEnum;
import cn.lycoder.boot.module.system.service.captcha.CaptchaService;
import cn.lycoder.boot.module.system.service.logger.LoginLogService;
import cn.lycoder.boot.module.system.service.online.OnlineSessionService;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_LOGIN_ACCOUNT_LOCKED;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_LOGIN_BAD_CREDENTIALS;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_LOGIN_USER_DISABLED;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_UPDATE_PASSWORD_OLD_ERROR;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_UPDATE_PASSWORD_SAME;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 认证 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * 代理头未透传真实 IP 时的占位值（Nginx 等对未知来源常填字面量 unknown）
     */
    private static final String UNKNOWN = "unknown";

    private final SystemUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;
    private final LoginAttemptService loginAttemptService;
    private final LoginLogService loginLogService;
    /**
     * 全局时间源（yukit.zone-id 配置），避免无参 now() 随 JVM 默认时区漂移
     */
    private final Clock clock;

    @Override
    public AuthLoginRespVO login(AuthLoginReqVO reqVO, HttpServletRequest request) {
        String username = reqVO.getUsername();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // 0. 优先校验图形验证码（置于密码校验前，先拦截机器暴力破解；开关关闭时自动跳过）
        captchaService.validate(reqVO.getUuid(), reqVO.getCode());

        // 1. 锁定校验先于密码：锁定期直接拒绝，持续爆破不再触达数据库
        if (loginAttemptService.isLocked(username)) {
            throw rejectLocked(username, clientIp, userAgent);
        }

        // 2. 查询用户，校验账号密码
        SystemUserDO user = userMapper.selectOne(Wrappers.<SystemUserDO>lambdaQuery()
                .eq(SystemUserDO::getUserName, username));
        if (user == null || !passwordEncoder.matches(reqVO.getPassword(), user.getPassword())) {
            // 用户不存在与密码错误返回同一错误码，避免账号枚举
            throw rejectBadCredentials(user, username, clientIp, userAgent);
        }

        // 3. 校验账号状态
        if (SystemUserDO.STATUS_DISABLE.equals(user.getStatus())) {
            record(user.getUserId(), username, LoginResultTypeEnum.USER_DISABLED, "账号被禁用", clientIp, userAgent);
            throw new ServiceException(AUTH_LOGIN_USER_DISABLED);
        }

        // 4. 登录成功：清除失败计数，Sa-Token 签发令牌
        loginAttemptService.reset(username);
        StpUtil.login(user.getUserId());
        // 会话元数据写入 token session，供在线会话管理展示（IP/UA/登录时间）
        writeOnlineSessionMeta(clientIp, userAgent);

        // 5. 记录最后登录 IP 与时间
        updateLoginInfo(user.getUserId(), clientIp);

        // 6. 写入登录日志并组装响应
        record(user.getUserId(), username, LoginResultTypeEnum.SUCCESS, "登录成功", clientIp, userAgent);
        return new AuthLoginRespVO(user.getUserId(),
                StpUtil.getTokenName(), StpUtil.getTokenValue());
    }

    /**
     * 账号处于锁定期：记日志并构造带剩余时间的拒绝异常
     */
    private ServiceException rejectLocked(String username, String clientIp, String userAgent) {
        long remainMinutes = loginAttemptService.getLockRemainMinutes(username);
        record(null, username, LoginResultTypeEnum.ACCOUNT_LOCKED,
                "账号已锁定，约 " + remainMinutes + " 分钟后解锁", clientIp, userAgent);
        return new ServiceException(AUTH_LOGIN_ACCOUNT_LOCKED.getCode(),
                AUTH_LOGIN_ACCOUNT_LOCKED.getMsg() + "，请于约 " + remainMinutes + " 分钟后重试");
    }

    /**
     * 账号密码校验失败：累加失败计数（触达阈值即锁定）并记日志
     */
    private ServiceException rejectBadCredentials(SystemUserDO user, String username,
                                                 String clientIp, String userAgent) {
        int failCount = loginAttemptService.recordFailure(username);
        record(user == null ? null : user.getUserId(), username, LoginResultTypeEnum.BAD_CREDENTIALS,
                "账号或密码错误（连续失败 " + failCount + " 次）", clientIp, userAgent);
        return new ServiceException(AUTH_LOGIN_BAD_CREDENTIALS);
    }

    /**
     * 将登录会话元数据写入当前 token 的 SaSession，键定义见 {@link OnlineSessionService}
     */
    private void writeOnlineSessionMeta(String clientIp, String userAgent) {
        StpUtil.getTokenSession()
                .set(OnlineSessionService.ATTR_LOGIN_IP, clientIp)
                .set(OnlineSessionService.ATTR_USER_AGENT, userAgent)
                .set(OnlineSessionService.ATTR_LOGIN_TIME, clock.millis());
    }

    /**
     * 更新最后登录 IP 与时间（局部更新，避免整行覆盖）
     */
    private void updateLoginInfo(Long userId, String clientIp) {
        SystemUserDO updateObj = new SystemUserDO();
        updateObj.setUserId(userId);
        updateObj.setLoginIp(clientIp);
        updateObj.setLoginDate(LocalDateTime.now(clock));
        userMapper.updateById(updateObj);
    }

    /**
     * 写入登录日志：审计失败绝不影响登录主流程
     */
    private void record(Long userId, String username, LoginResultTypeEnum result,
                        String msg, String clientIp, String userAgent) {
        try {
            loginLogService.createLoginLog(userId, username, result, msg, clientIp, userAgent);
        } catch (Exception e) {
            log.warn("[login] 写入登录日志失败: username={}, result={}", username, result, e);
        }
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    @IgnoreDataPermission
    public void updatePassword(AuthUpdatePasswordReqVO reqVO) {
        // 会话不可能无主，防御性校验用户仍存在（如管理员已删号但未过期会话）
        Long userId = StpUtil.getLoginIdAsLong();
        SystemUserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(USER_NOT_EXISTS);
        }
        if (!passwordEncoder.matches(reqVO.getOldPassword(), user.getPassword())) {
            throw new ServiceException(AUTH_UPDATE_PASSWORD_OLD_ERROR);
        }
        // 与存量密码比对，拦截新旧相同（改后必踢线，同密码重设没有意义还徒增断线）
        if (passwordEncoder.matches(reqVO.getNewPassword(), user.getPassword())) {
            throw new ServiceException(AUTH_UPDATE_PASSWORD_SAME);
        }
        // 只更新密码列，updateBy/updateTime 由 MetaObjectHandler 填充
        SystemUserDO updateObj = new SystemUserDO();
        updateObj.setUserId(userId);
        updateObj.setPassword(passwordEncoder.encode(reqVO.getNewPassword()));
        userMapper.updateById(updateObj);
        // 踢出该账号全部会话（含当前设备）：凭证变更后旧令牌一律失效，强制用新密码重登
        StpUtil.kickout(userId);
    }

    /**
     * 获取客户端真实 IP，优先取代理透传头
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多级代理时取第一个非 unknown 的 IP
            for (String candidate : ip.split(",")) {
                if (!UNKNOWN.equalsIgnoreCase(candidate.trim())) {
                    return candidate.trim();
                }
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}

package cn.lycoder.boot.module.system.service.auth;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import cn.lycoder.boot.module.system.service.captcha.CaptchaService;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_LOGIN_BAD_CREDENTIALS;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.AUTH_LOGIN_USER_DISABLED;

/**
 * 认证 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SystemUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;

    @Override
    public AuthLoginRespVO login(AuthLoginReqVO reqVO, HttpServletRequest request) {
        // 0. 优先校验图形验证码（置于密码校验前，先拦截机器暴力破解；开关关闭时自动跳过）
        captchaService.validate(reqVO.getUuid(), reqVO.getCode());

        // 1. 查询用户，校验账号密码
        SystemUserDO user = userMapper.selectOne(Wrappers.<SystemUserDO>lambdaQuery()
                .eq(SystemUserDO::getUserName, reqVO.getUsername()));
        if (user == null || !passwordEncoder.matches(reqVO.getPassword(), user.getPassword())) {
            // 用户不存在与密码错误返回同一错误码，避免账号枚举
            throw new ServiceException(AUTH_LOGIN_BAD_CREDENTIALS);
        }

        // 2. 校验账号状态
        if (SystemUserDO.STATUS_DISABLE.equals(user.getStatus())) {
            throw new ServiceException(AUTH_LOGIN_USER_DISABLED);
        }

        // 3. Sa-Token 登录，生成令牌
        StpUtil.login(user.getUserId());

        // 4. 记录最后登录 IP 与时间
        SystemUserDO updateObj = new SystemUserDO();
        updateObj.setUserId(user.getUserId());
        updateObj.setLoginIp(getClientIp(request));
        updateObj.setLoginDate(LocalDateTime.now());
        userMapper.updateById(updateObj);

        // 5. 组装响应
        return new AuthLoginRespVO(user.getUserId(),
                StpUtil.getTokenName(), StpUtil.getTokenValue());
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取客户端真实 IP，优先取代理透传头
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            // 多级代理时取第一个非 unknown 的 IP
            for (String candidate : ip.split(",")) {
                if (!"unknown".equalsIgnoreCase(candidate.trim())) {
                    return candidate.trim();
                }
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}

package cn.lycoder.boot.module.system.service.auth;

import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证 Service 接口
 */
public interface AuthService {

    /**
     * 账号密码登录
     *
     * @param reqVO   登录请求
     * @param request 当前请求（用于记录登录 IP）
     * @return 令牌信息
     */
    AuthLoginRespVO login(AuthLoginReqVO reqVO, HttpServletRequest request);

    /**
     * 退出登录
     */
    void logout();
}

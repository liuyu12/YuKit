package cn.lycoder.boot.module.system.controller.admin.auth;

import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.framework.protection.ratelimiter.LimitType;
import cn.lycoder.boot.framework.protection.ratelimiter.RateLimiter;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthCaptchaRespVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthUpdatePasswordReqVO;
import cn.lycoder.boot.module.system.service.auth.AuthService;
import cn.lycoder.boot.module.system.service.captcha.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台 - 认证
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/system/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    /**
     * 获取登录图形验证码
     * <p>
     * 该路径已配置在免登录白名单 yukit.security.exclude-paths 中
     */
    @Operation(summary = "获取登录图形验证码")
    @GetMapping("/captcha")
    public Result<AuthCaptchaRespVO> captcha() {
        return Result.success(captchaService.generate());
    }

    /**
     * 账号密码登录
     * <p>
     * 该路径已配置在免登录白名单 yukit.security.exclude-paths 中
     */
    @Operation(summary = "账号密码登录")
    @RateLimiter(count = 10, time = 60, limitType = LimitType.IP, message = "登录尝试过于频繁，请稍后再试")
    @PostMapping("/login")
    public Result<AuthLoginRespVO> login(@RequestBody @Valid AuthLoginReqVO reqVO,
                                         HttpServletRequest request) {
        return Result.success(authService.login(reqVO, request));
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Boolean> logout() {
        authService.logout();
        return Result.success(true);
    }

    /**
     * 自助修改密码（校验旧密码，成功后全端踢下线）
     * <p>
     * recordParams=false：审计切面不得把旧/新密码明文写入 sys_operate_log
     */
    @Operation(summary = "修改当前账号密码")
    @OperationLog(module = "认证管理", name = "修改密码", type = OperationType.UPDATE, recordParams = false)
    @PutMapping("/update-password")
    public Result<Boolean> updatePassword(@RequestBody @Valid AuthUpdatePasswordReqVO reqVO) {
        authService.updatePassword(reqVO);
        return Result.success(true);
    }
}

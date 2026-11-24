package cn.lycoder.boot.framework.security.core.handler;

import cn.lycoder.boot.framework.common.enums.GlobalErrorCodeConstants;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 异常统一处理器
 * <p>
 * 将鉴权异常翻译为项目统一的 {@link Result} 响应结构。
 * <p>
 * Order 必须高于 web starter 的 GlobalExceptionHandler（其 Exception 兜底会吞掉未登录异常），
 * 多个 Advice 按优先级逐个匹配，命中的 Advice 内部再按异常类型择优。
 */
@Slf4j
@Order(-100)
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 未登录：按 Sa-Token 的 type 细分场景，提示更友好
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("[handleNotLoginException] 未登录访问，type={}", e.getType());
        return Result.error(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未登录，请先登录";
            case NotLoginException.INVALID_TOKEN -> "登录凭证无效，请重新登录";
            case NotLoginException.TOKEN_TIMEOUT -> "登录已过期，请重新登录";
            case NotLoginException.BE_REPLACED -> "账号已在其他地方登录";
            case NotLoginException.KICK_OUT -> "账号已被强制下线";
            default -> GlobalErrorCodeConstants.UNAUTHORIZED.getMsg();
        });
    }

    /**
     * 缺少权限
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("[handleNotPermissionException] 缺少权限：{}", e.getPermission());
        return Result.error(GlobalErrorCodeConstants.FORBIDDEN);
    }

    /**
     * 缺少角色
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("[handleNotRoleException] 缺少角色：{}", e.getRole());
        return Result.error(GlobalErrorCodeConstants.FORBIDDEN);
    }
}

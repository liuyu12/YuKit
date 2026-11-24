package cn.lycoder.boot.module.system.controller.admin.online;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.online.vo.OnlineSessionRespVO;
import cn.lycoder.boot.module.system.service.online.OnlineSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 在线会话管理
 * <p>
 * 展示当前持有有效令牌的用户会话，支持按会话令牌或用户维度强制下线（Sa-Token 踢下线）。
 */
@Tag(name = "在线会话管理")
@RestController
@RequestMapping("/system/online-sessions")
@RequiredArgsConstructor
public class OnlineSessionController {

    private final OnlineSessionService onlineSessionService;

    /**
     * 查询在线会话列表
     */
    @Operation(summary = "查询在线会话列表")
    @GetMapping
    @SaCheckPermission("system:online:query")
    public Result<List<OnlineSessionRespVO>> getOnlineSessionList() {
        return Result.success(onlineSessionService.getOnlineSessionList());
    }

    /**
     * 强制指定会话下线
     */
    @Operation(summary = "强制指定会话下线")
    @OperationLog(module = "在线会话", name = "强制会话下线", type = OperationType.FORCE)
    @DeleteMapping("/{tokenValue}")
    @SaCheckPermission("system:online:force-logout")
    public Result<Boolean> forceLogout(
            @Parameter(description = "会话令牌") @PathVariable("tokenValue") String tokenValue) {
        onlineSessionService.forceLogout(tokenValue);
        return Result.success(true);
    }

    /**
     * 强制某用户全部会话下线
     */
    @Operation(summary = "强制某用户全部会话下线")
    @OperationLog(module = "在线会话", name = "强制用户下线", type = OperationType.FORCE)
    @DeleteMapping("/users/{userId}")
    @SaCheckPermission("system:online:force-logout")
    public Result<Boolean> forceLogoutByUserId(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        onlineSessionService.forceLogoutByUserId(userId);
        return Result.success(true);
    }
}

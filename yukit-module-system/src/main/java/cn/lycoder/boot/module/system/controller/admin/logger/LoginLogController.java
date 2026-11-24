package cn.lycoder.boot.module.system.controller.admin.logger;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.logger.vo.LoginLogPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.logger.vo.LoginLogRespVO;
import cn.lycoder.boot.module.system.convert.logger.LoginLogConvert;
import cn.lycoder.boot.module.system.dal.dataobject.logger.SystemLoginLogDO;
import cn.lycoder.boot.module.system.service.logger.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志管理
 * <p>
 * 记录由登录流程自动写入，本控制器仅提供查询与维护。
 */
@Tag(name = "登录日志管理")
@RestController
@RequestMapping("/system/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;
    private final LoginLogConvert loginLogConvert;

    /**
     * 分页查询登录日志
     */
    @Operation(summary = "分页查询登录日志")
    @GetMapping
    @SaCheckPermission("monitor:logininfor:query")
    public Result<PageResult<LoginLogRespVO>> getLoginLogPage(@Valid LoginLogPageReqVO reqVO) {
        PageResult<SystemLoginLogDO> pageResult = loginLogService.getLoginLogPage(reqVO);
        return Result.success(loginLogConvert.convertPage(pageResult));
    }

    /**
     * 获取登录日志详情
     */
    @Operation(summary = "获取登录日志详情")
    @GetMapping("/{id}")
    @SaCheckPermission("monitor:logininfor:query")
    public Result<LoginLogRespVO> getLoginLog(@Parameter(description = "日志ID") @PathVariable("id") Long id) {
        SystemLoginLogDO loginLog = loginLogService.getLoginLog(id);
        return Result.success(loginLog == null ? null : loginLogConvert.convert(loginLog));
    }

    /**
     * 删除登录日志
     */
    @Operation(summary = "删除登录日志")
    @OperationLog(module = "登录日志", name = "删除登录日志", type = OperationType.DELETE)
    @DeleteMapping("/{id}")
    @SaCheckPermission("monitor:logininfor:remove")
    public Result<Boolean> deleteLoginLog(@Parameter(description = "日志ID") @PathVariable("id") Long id) {
        loginLogService.deleteLoginLog(id);
        return Result.success(true);
    }
}

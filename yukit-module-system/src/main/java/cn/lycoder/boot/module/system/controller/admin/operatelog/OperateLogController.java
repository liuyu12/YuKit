package cn.lycoder.boot.module.system.controller.admin.operatelog;

import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.operatelog.vo.OperateLogPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.operatelog.vo.OperateLogRespVO;
import cn.lycoder.boot.module.system.convert.operatelog.OperateLogConvert;
import cn.lycoder.boot.module.system.dal.dataobject.operatelog.SystemOperateLogDO;
import cn.lycoder.boot.module.system.service.operatelog.OperateLogService;
import cn.dev33.satoken.annotation.SaCheckPermission;
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
 * 操作日志管理
 * <p>
 * 记录由 {@code @OperationLog} 切面自动写入，本控制器仅提供查询与维护。
 */
@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/system/operate-logs")
@RequiredArgsConstructor
public class OperateLogController {

    private final OperateLogService operateLogService;
    private final OperateLogConvert operateLogConvert;

    /**
     * 分页查询操作日志
     */
    @Operation(summary = "分页查询操作日志")
    @GetMapping
    @SaCheckPermission("system:operate-log:query")
    public Result<PageResult<OperateLogRespVO>> getOperateLogPage(@Valid OperateLogPageReqVO reqVO) {
        PageResult<SystemOperateLogDO> pageResult = operateLogService.getOperateLogPage(reqVO);
        return Result.success(operateLogConvert.convertPage(pageResult));
    }

    /**
     * 获取操作日志详情
     */
    @Operation(summary = "获取操作日志详情")
    @GetMapping("/{id}")
    @SaCheckPermission("system:operate-log:query")
    public Result<OperateLogRespVO> getOperateLog(@Parameter(description = "日志ID") @PathVariable("id") Long id) {
        SystemOperateLogDO operateLog = operateLogService.getOperateLog(id);
        return Result.success(operateLog == null ? null : operateLogConvert.convert(operateLog));
    }

    /**
     * 删除操作日志
     */
    @Operation(summary = "删除操作日志")
    @OperationLog(module = "操作日志", name = "删除操作日志", type = OperationType.DELETE)
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:operate-log:delete")
    public Result<Boolean> deleteOperateLog(@Parameter(description = "日志ID") @PathVariable("id") Long id) {
        operateLogService.deleteOperateLog(id);
        return Result.success(true);
    }
}

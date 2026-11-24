package cn.lycoder.boot.module.system.controller.admin.job;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobLogPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobLogRespVO;
import cn.lycoder.boot.module.system.convert.job.JobLogConvert;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobLogDO;
import cn.lycoder.boot.module.system.service.job.JobLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * 定时任务执行日志查询与维护
 */
@Tag(name = "定时任务执行日志")
@Validated
@RestController
@RequestMapping("/monitor/job-logs")
@RequiredArgsConstructor
public class JobLogController {

    private final JobLogService jobLogService;
    private final JobLogConvert jobLogConvert;

    @Operation(summary = "分页查询执行日志")
    @GetMapping
    @SaCheckPermission("monitor:job:query")
    public Result<PageResult<JobLogRespVO>> getJobLogPage(@Valid JobLogPageReqVO reqVO) {
        PageResult<SystemJobLogDO> page = jobLogService.getJobLogPage(reqVO);
        return Result.success(jobLogConvert.convertPage(page));
    }

    @Operation(summary = "获取执行日志详情")
    @GetMapping("/{jobLogId}")
    @SaCheckPermission("monitor:job:query")
    public Result<JobLogRespVO> getJobLog(
            @Parameter(description = "日志ID") @PathVariable("jobLogId") Long jobLogId) {
        SystemJobLogDO log = jobLogService.getJobLog(jobLogId);
        return Result.success(log == null ? null : jobLogConvert.convert(log));
    }

    @Operation(summary = "删除执行日志")
    @OperationLog(module = "定时任务", name = "删除执行日志", type = OperationType.DELETE)
    @DeleteMapping("/{jobLogId}")
    @SaCheckPermission("monitor:job:remove")
    public Result<Boolean> deleteJobLog(
            @Parameter(description = "日志ID") @PathVariable("jobLogId") Long jobLogId) {
        jobLogService.deleteJobLog(jobLogId);
        return Result.success(true);
    }

    @Operation(summary = "批量删除执行日志")
    @OperationLog(module = "定时任务", name = "批量删除执行日志", type = OperationType.DELETE)
    @DeleteMapping
    @SaCheckPermission("monitor:job:remove")
    public Result<Boolean> deleteJobLogByIds(
            @Parameter(description = "日志ID列表") @RequestParam("jobLogIds")
            @NotNull(message = "日志ID列表不能为空") Collection<Long> jobLogIds) {
        jobLogService.deleteJobLogByIds(jobLogIds);
        return Result.success(true);
    }

    @Operation(summary = "清空执行日志")
    @OperationLog(module = "定时任务", name = "清空执行日志", type = OperationType.OTHER)
    @DeleteMapping("/clean")
    @SaCheckPermission("monitor:job:remove")
    public Result<Boolean> cleanJobLog() {
        jobLogService.cleanJobLog();
        return Result.success(true);
    }
}

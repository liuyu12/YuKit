package cn.lycoder.boot.module.system.controller.admin.job;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.framework.protection.idempotent.Idempotent;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobRespVO;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobUpdateReqVO;
import cn.lycoder.boot.module.system.convert.job.JobConvert;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobDO;
import cn.lycoder.boot.module.system.service.job.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * 定时任务管理：CRUD + 启停 + 立即执行
 */
@Tag(name = "定时任务管理")
@Validated
@RestController
@RequestMapping("/monitor/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobConvert jobConvert;

    @Operation(summary = "创建定时任务")
    @OperationLog(module = "定时任务", name = "创建定时任务", type = OperationType.INSERT)
    @Idempotent(timeout = 3, message = "请勿重复提交")
    @PostMapping
    @SaCheckPermission("monitor:job:add")
    public Result<Long> createJob(@Valid @RequestBody JobCreateReqVO reqVO) {
        return Result.success(jobService.createJob(reqVO));
    }

    @Operation(summary = "更新定时任务")
    @OperationLog(module = "定时任务", name = "更新定时任务", type = OperationType.UPDATE)
    @PutMapping
    @SaCheckPermission("monitor:job:edit")
    public Result<Boolean> updateJob(@Valid @RequestBody JobUpdateReqVO reqVO) {
        jobService.updateJob(reqVO);
        return Result.success(true);
    }

    @Operation(summary = "删除定时任务")
    @OperationLog(module = "定时任务", name = "删除定时任务", type = OperationType.DELETE)
    @DeleteMapping("/{jobId}")
    @SaCheckPermission("monitor:job:remove")
    public Result<Boolean> deleteJob(@Parameter(description = "任务ID") @PathVariable("jobId") Long jobId) {
        jobService.deleteJob(jobId);
        return Result.success(true);
    }

    @Operation(summary = "批量删除定时任务")
    @OperationLog(module = "定时任务", name = "批量删除定时任务", type = OperationType.DELETE)
    @DeleteMapping
    @SaCheckPermission("monitor:job:remove")
    public Result<Boolean> deleteJobByIds(
            @Parameter(description = "任务ID列表") @RequestParam("jobIds") @NotNull(message = "任务ID列表不能为空")
            Collection<Long> jobIds) {
        jobService.deleteJobByIds(jobIds);
        return Result.success(true);
    }

    @Operation(summary = "分页查询定时任务")
    @GetMapping
    @SaCheckPermission("monitor:job:list")
    public Result<PageResult<JobRespVO>> getJobPage(@Valid JobPageReqVO reqVO) {
        PageResult<SystemJobDO> page = jobService.getJobPage(reqVO);
        PageResult<JobRespVO> voPage = jobConvert.convertPage(page);
        // 填充下次触发时间（基于 cron 估算）
        List<JobRespVO> list = voPage.getList();
        list.forEach(vo -> vo.setNextFireTime(jobService.getNextFireTime(vo.getCronExpression())));
        return Result.success(new PageResult<>(list, voPage.getTotal()));
    }

    @Operation(summary = "获取定时任务详情")
    @GetMapping("/{jobId}")
    @SaCheckPermission("monitor:job:query")
    public Result<JobRespVO> getJob(@Parameter(description = "任务ID") @PathVariable("jobId") Long jobId) {
        SystemJobDO job = jobService.getJob(jobId);
        if (job == null) {
            return Result.success(null);
        }
        JobRespVO vo = jobConvert.convert(job);
        vo.setNextFireTime(jobService.getNextFireTime(job.getCronExpression()));
        return Result.success(vo);
    }

    @Operation(summary = "启停定时任务")
    @OperationLog(module = "定时任务", name = "启停定时任务", type = OperationType.UPDATE)
    @PutMapping("/change-status")
    @SaCheckPermission("monitor:job:changeStatus")
    public Result<Boolean> changeStatus(
            @Parameter(description = "任务ID") @RequestParam("jobId") @NotNull(message = "任务ID不能为空") Long jobId,
            @Parameter(description = "状态：0 正常 / 1 暂停") @RequestParam("status")
            @NotBlank(message = "状态不能为空") String status) {
        jobService.changeStatus(jobId, status);
        return Result.success(true);
    }

    @Operation(summary = "立即执行定时任务")
    @OperationLog(module = "定时任务", name = "立即执行定时任务", type = OperationType.OTHER)
    @PostMapping("/run/{jobId}")
    @SaCheckPermission("monitor:job:run")
    public Result<Boolean> runJob(@Parameter(description = "任务ID") @PathVariable("jobId") Long jobId) {
        jobService.runJob(jobId);
        return Result.success(true);
    }
}

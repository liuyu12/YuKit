package cn.lycoder.boot.module.system.service.job;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobLogPageReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobLogDO;
import cn.lycoder.boot.module.system.dal.mapper.job.SystemJobLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 定时任务执行日志 Service 实现
 */
@Service
@RequiredArgsConstructor
public class JobLogServiceImpl implements JobLogService {

    /**
     * 异常栈最大保留长度，超出部分截断
     */
    private static final int MAX_EXCEPTION_LENGTH = 2000;
    /**
     * 摘要最大保留长度
     */
    private static final int MAX_MESSAGE_LENGTH = 500;

    private final SystemJobLogMapper jobLogMapper;
    /**
     * 全局时间源（yukit.zone-id 配置）
     */
    private final Clock clock;

    @Override
    public PageResult<SystemJobLogDO> getJobLogPage(JobLogPageReqVO reqVO) {
        LambdaQueryWrapper<SystemJobLogDO> wrapper = Wrappers.<SystemJobLogDO>lambdaQuery()
                .eq(reqVO.getJobId() != null, SystemJobLogDO::getJobId, reqVO.getJobId())
                .like(StringUtils.hasText(reqVO.getJobName()), SystemJobLogDO::getJobName, reqVO.getJobName())
                .eq(StringUtils.hasText(reqVO.getJobGroup()), SystemJobLogDO::getJobGroup, reqVO.getJobGroup())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemJobLogDO::getStatus, reqVO.getStatus())
                .ge(reqVO.getBeginTime() != null, SystemJobLogDO::getStartTime, reqVO.getBeginTime())
                .le(reqVO.getEndTime() != null, SystemJobLogDO::getStartTime, reqVO.getEndTime())
                .orderByDesc(SystemJobLogDO::getJobLogId);
        Page<SystemJobLogDO> page = jobLogMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public SystemJobLogDO getJobLog(Long jobLogId) {
        return jobLogMapper.selectById(jobLogId);
    }

    @Override
    public void createJobLog(SystemJobLogDO jobLog) {
        if (jobLog.getCreateTime() == null) {
            jobLog.setCreateTime(LocalDateTime.now(clock));
        }
        truncateLongFields(jobLog);
        jobLogMapper.insert(jobLog);
    }

    @Override
    public void deleteJobLog(Long jobLogId) {
        jobLogMapper.deleteById(jobLogId);
    }

    @Override
    public void deleteJobLogByIds(Collection<Long> jobLogIds) {
        if (CollectionUtils.isEmpty(jobLogIds)) {
            return;
        }
        jobLogMapper.deleteByIds(jobLogIds);
    }

    @Override
    public void cleanJobLog() {
        // 全表清空：用 delete 空条件触发 truncate 优化；MP delete 空 wrapper 直接 DELETE FROM 不带 WHERE
        jobLogMapper.delete(Wrappers.emptyWrapper());
    }

    private static void truncateLongFields(SystemJobLogDO jobLog) {
        if (jobLog.getExceptionInfo() != null && jobLog.getExceptionInfo().length() > MAX_EXCEPTION_LENGTH) {
            jobLog.setExceptionInfo(jobLog.getExceptionInfo().substring(0, MAX_EXCEPTION_LENGTH));
        }
        if (jobLog.getJobMessage() != null && jobLog.getJobMessage().length() > MAX_MESSAGE_LENGTH) {
            jobLog.setJobMessage(jobLog.getJobMessage().substring(0, MAX_MESSAGE_LENGTH));
        }
    }
}

package cn.lycoder.boot.module.system.service.job;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobCreateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobDO;
import cn.lycoder.boot.module.system.dal.mapper.job.SystemJobMapper;
import cn.lycoder.boot.module.system.framework.job.annotation.JobHandler;
import cn.lycoder.boot.module.system.framework.job.core.JobScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link JobServiceImpl} 校验 + 同步逻辑单元测试（纯 Mockito，不起 Spring）
 */
class JobServiceImplTest {

    private SystemJobMapper jobMapper;
    private JobScheduler jobScheduler;
    private ApplicationContext applicationContext;
    private JobServiceImpl service;

    @BeforeEach
    void setUp() {
        jobMapper = mock(SystemJobMapper.class);
        jobScheduler = mock(JobScheduler.class);
        applicationContext = mock(ApplicationContext.class);
        service = new JobServiceImpl(jobMapper, jobScheduler, applicationContext);
    }

    /** 提供合法 invoke_target 校验用的目标 Bean */
    static class DemoTarget {
        @JobHandler("demo")
        public void beat() {
        }

        @JobHandler("demo with arg")
        public void echo(String message) {
        }

        public void notAnnotated() {
        }
    }

    private void stubBeanOk() {
        when(applicationContext.getBean(anyString())).thenReturn(new DemoTarget());
    }

    private JobCreateReqVO baseReq() {
        JobCreateReqVO req = new JobCreateReqVO();
        req.setJobName("demo");
        req.setJobGroup("DEFAULT");
        req.setInvokeTarget("demoTarget.beat");
        req.setCronExpression("0 0/1 * * * ?");
        req.setStatus(SystemJobDO.STATUS_NORMAL);
        req.setConcurrent(SystemJobDO.CONCURRENT_FORBID);
        req.setMisfirePolicy(SystemJobDO.MISFIRE_DO_NOTHING);
        return req;
    }

    @Test
    @DisplayName("createJob：cron 非法直接拒绝，不落库")
    void rejectsInvalidCron() {
        JobCreateReqVO req = baseReq();
        req.setCronExpression("not a cron");
        assertThatThrownBy(() -> service.createJob(req))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 1002006002);
        verify(jobMapper, never()).insert(any(SystemJobDO.class));
    }

    @Test
    @DisplayName("createJob：Bean 不存在拒绝，提示统一不泄露内部结构")
    void rejectsMissingBean() {
        when(applicationContext.getBean(anyString())).thenThrow(new RuntimeException("no such bean"));
        JobCreateReqVO req = baseReq();
        assertThatThrownBy(() -> service.createJob(req))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 1002006003);
    }

    @Test
    @DisplayName("createJob：方法未标注 @JobHandler 拒绝")
    void rejectsUnannotatedMethod() {
        stubBeanOk();
        JobCreateReqVO req = baseReq();
        req.setInvokeTarget("demoTarget.notAnnotated");
        assertThatThrownBy(() -> service.createJob(req))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 1002006003);
    }

    @Test
    @DisplayName("createJob：合法入参落库并注册调度")
    void createsAndRegisters() {
        stubBeanOk();
        when(jobMapper.selectCount(any())).thenReturn(0L);
        when(jobMapper.insert(any(SystemJobDO.class))).thenAnswer(inv -> {
            SystemJobDO arg = inv.getArgument(0);
            arg.setJobId(101L);
            return 1;
        });
        Long id = service.createJob(baseReq());
        assertThat(id).isEqualTo(101L);
        verify(jobMapper).insert(any(SystemJobDO.class));
        verify(jobScheduler).addOrUpdateJob(any(SystemJobDO.class));
    }

    @Test
    @DisplayName("changeStatus：切暂停 → 取消 future")
    void pauseCancelsFuture() {
        SystemJobDO existing = new SystemJobDO();
        existing.setJobId(1L);
        existing.setStatus(SystemJobDO.STATUS_NORMAL);
        when(jobMapper.selectById(1L)).thenReturn(existing);
        service.changeStatus(1L, SystemJobDO.STATUS_PAUSE);
        verify(jobScheduler).pauseJob(1L);
        verify(jobScheduler, never()).addOrUpdateJob(any());
    }

    @Test
    @DisplayName("changeStatus：切正常 → 重新注册调度")
    void resumeRegisters() {
        SystemJobDO existing = new SystemJobDO();
        existing.setJobId(1L);
        existing.setStatus(SystemJobDO.STATUS_PAUSE);
        when(jobMapper.selectById(1L)).thenReturn(existing);
        service.changeStatus(1L, SystemJobDO.STATUS_NORMAL);
        verify(jobScheduler).addOrUpdateJob(any(SystemJobDO.class));
    }

    @Test
    @DisplayName("changeStatus：未知状态码抛业务异常")
    void rejectsUnknownStatus() {
        assertThatThrownBy(() -> service.changeStatus(1L, "9"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("syncAllOnStartup：加载所有 status=0 任务并注册")
    void syncLoadsNormalJobs() {
        SystemJobDO j1 = new SystemJobDO();
        j1.setJobId(1L);
        j1.setStatus(SystemJobDO.STATUS_NORMAL);
        SystemJobDO j2 = new SystemJobDO();
        j2.setJobId(2L);
        j2.setStatus(SystemJobDO.STATUS_NORMAL);
        when(jobMapper.selectList(any())).thenReturn(List.of(j1, j2));
        service.syncAllOnStartup();
        verify(jobScheduler, times(2)).addOrUpdateJob(any(SystemJobDO.class));
    }

    @Test
    @DisplayName("getNextFireTime：cron 合法返回未来时间，非法返回 null")
    void calculatesNextFireTime() {
        assertThat(service.getNextFireTime("0 0 3 * * ?")).isNotNull();
        assertThat(service.getNextFireTime("bad")).isNull();
        assertThat(service.getNextFireTime(null)).isNull();
    }
}

package cn.lycoder.boot.module.system.convert.job;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobLogRespVO;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 定时任务执行日志转换器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobLogConvert {

    JobLogRespVO convert(SystemJobLogDO log);

    default PageResult<JobLogRespVO> convertPage(PageResult<SystemJobLogDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

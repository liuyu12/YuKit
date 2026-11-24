package cn.lycoder.boot.module.system.convert.job;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.job.vo.JobRespVO;
import cn.lycoder.boot.module.system.dal.dataobject.job.SystemJobDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 定时任务转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobConvert {

    JobRespVO convert(SystemJobDO job);

    List<JobRespVO> convertList(List<SystemJobDO> jobs);

    default PageResult<JobRespVO> convertPage(PageResult<SystemJobDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

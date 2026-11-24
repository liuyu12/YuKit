package cn.lycoder.boot.module.system.convert.operatelog;

import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.operatelog.vo.OperateLogRespVO;
import cn.lycoder.boot.module.system.dal.dataobject.operatelog.SystemOperateLogDO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.List;

/**
 * 操作日志转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OperateLogConvert {

    OperateLogRespVO convert(SystemOperateLogDO operateLog);

    List<OperateLogRespVO> convertList(List<SystemOperateLogDO> operateLogs);

    default PageResult<OperateLogRespVO> convertPage(PageResult<SystemOperateLogDO> page) {
        return PageResult.convert(page, this::convert);
    }

    /**
     * 依据操作类型码回填中文描述，便于前端直接展示
     */
    @AfterMapping
    default void fillTypeDescription(SystemOperateLogDO source, @MappingTarget OperateLogRespVO target) {
        if (source.getType() == null) {
            return;
        }
        Arrays.stream(OperationType.values())
                .filter(type -> type.getCode() == source.getType())
                .findFirst()
                .ifPresent(type -> target.setTypeDescription(type.getDescription()));
    }
}

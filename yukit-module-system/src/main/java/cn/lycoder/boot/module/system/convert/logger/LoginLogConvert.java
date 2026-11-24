package cn.lycoder.boot.module.system.convert.logger;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.logger.vo.LoginLogRespVO;
import cn.lycoder.boot.module.system.dal.dataobject.logger.SystemLoginLogDO;
import cn.lycoder.boot.module.system.enums.logger.LoginResultTypeEnum;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.List;

/**
 * 登录日志转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoginLogConvert {

    LoginLogRespVO convert(SystemLoginLogDO loginLog);

    List<LoginLogRespVO> convertList(List<SystemLoginLogDO> loginLogs);

    default PageResult<LoginLogRespVO> convertPage(PageResult<SystemLoginLogDO> page) {
        return PageResult.convert(page, this::convert);
    }

    /**
     * 依据登录结果码回填中文描述，便于前端直接展示
     */
    @AfterMapping
    default void fillResultDescription(SystemLoginLogDO source, @MappingTarget LoginLogRespVO target) {
        if (source.getResult() == null) {
            return;
        }
        Arrays.stream(LoginResultTypeEnum.values())
                .filter(type -> type.getCode() == source.getResult())
                .findFirst()
                .ifPresent(type -> target.setResultDescription(type.getDescription()));
    }
}

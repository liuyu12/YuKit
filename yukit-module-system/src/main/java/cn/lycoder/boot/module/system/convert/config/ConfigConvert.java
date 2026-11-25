package cn.lycoder.boot.module.system.convert.config;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigRespVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.config.SystemConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 参数管理转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConfigConvert {

    /**
     * 创建：忽略主键与审计字段，防止 ReqVO 伪造注入；
     * configType/configKey/configValue 等 update VO 缺失字段在 DO 中为 null，updateById 时自动跳过
     */
    @Mapping(target = "configId", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemConfigDO convert(ConfigCreateReqVO reqVO);

    /**
     * 更新：保留 configId（由路径参数覆盖后随体传入），忽略审计字段
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemConfigDO convert(ConfigUpdateReqVO reqVO);

    ConfigRespVO convert(SystemConfigDO config);

    List<ConfigRespVO> convertList(List<SystemConfigDO> configs);

    default PageResult<ConfigRespVO> convertPage(PageResult<SystemConfigDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

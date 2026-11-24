package cn.lycoder.boot.module.system.convert.dict;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeRespVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.dict.SystemDictTypeDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 字典类型转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictTypeConvert {

    /**
     * 创建：忽略主键与审计字段，防止 ReqVO 伪造注入
     */
    @Mapping(target = "dictId", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemDictTypeDO convert(DictTypeCreateReqVO reqVO);

    /**
     * 更新：保留 dictId（由路径参数覆盖后随体传入），忽略审计字段
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemDictTypeDO convert(DictTypeUpdateReqVO reqVO);

    DictTypeRespVO convert(SystemDictTypeDO dictType);

    List<DictTypeRespVO> convertList(List<SystemDictTypeDO> dictTypes);

    default PageResult<DictTypeRespVO> convertPage(PageResult<SystemDictTypeDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

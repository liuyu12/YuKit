package cn.lycoder.boot.module.system.convert.dict;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataRespVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.dict.SystemDictDataDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 字典数据转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictDataConvert {

    /**
     * 创建：忽略主键与审计字段
     */
    @Mapping(target = "dictCode", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemDictDataDO convert(DictDataCreateReqVO reqVO);

    /**
     * 更新：保留 dictCode（路径参数覆盖后随体传入）
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemDictDataDO convert(DictDataUpdateReqVO reqVO);

    DictDataRespVO convert(SystemDictDataDO dictData);

    List<DictDataRespVO> convertList(List<SystemDictDataDO> dictDataList);

    default PageResult<DictDataRespVO> convertPage(PageResult<SystemDictDataDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

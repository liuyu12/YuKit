package cn.lycoder.boot.module.system.convert.file;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.file.vo.FileRespVO;
import cn.lycoder.boot.module.system.dal.dataobject.file.SystemFileDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 文件转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileConvert {

    FileRespVO convert(SystemFileDO file);

    List<FileRespVO> convertList(List<SystemFileDO> files);

    default PageResult<FileRespVO> convertPage(PageResult<SystemFileDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

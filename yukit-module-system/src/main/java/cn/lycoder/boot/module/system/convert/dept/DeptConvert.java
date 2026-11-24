package cn.lycoder.boot.module.system.convert.dept;

import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptRespVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.dept.SystemDeptDO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 部门对象转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeptConvert {

    /**
     * 忽略主键与 ancestors（服务层据父链计算），防止 ReqVO 伪造注入
     */
    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "deptId", ignore = true)
    @Mapping(target = "ancestors", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemDeptDO convert(DeptCreateReqVO reqVO);

    SystemDeptDO convert(DeptUpdateReqVO reqVO);

    DeptRespVO convert(SystemDeptDO dept);

    List<DeptRespVO> convertList(List<SystemDeptDO> depts);
}

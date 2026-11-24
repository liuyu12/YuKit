package cn.lycoder.boot.module.system.convert.role;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleRespVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.role.SystemRoleDO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 角色对象转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleConvert {

    /**
     * 明确忽略主键与审计字段，防止 ReqVO 伪造 id/createBy 注入；menuIds 属于授权关系不进 DO
     */
    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "dataScope", ignore = true)
    @Mapping(target = "menuCheckStrictly", ignore = true)
    @Mapping(target = "deptCheckStrictly", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemRoleDO convert(RoleCreateReqVO reqVO);

    SystemRoleDO convert(RoleUpdateReqVO reqVO);

    RoleRespVO convert(SystemRoleDO role);

    List<RoleRespVO> convertList(List<SystemRoleDO> roles);

    /**
     * 分页结果转换：DO 分页 → VO 分页
     */
    default PageResult<RoleRespVO> convertPage(PageResult<SystemRoleDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

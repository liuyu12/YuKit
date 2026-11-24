package cn.lycoder.boot.module.system.convert.user;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserRespVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 用户对象转换器（MapStruct 编译期生成实现，零反射开销）
 * <p>
 * unmappedTargetPolicy = IGNORE：ReqVO→DO 时 DO 的未匹配字段（userId、审计字段等）
 * 交由数据库默认值与 MetaObjectHandler 填充，无需映射也无需告警。
 * 敏感字段防泄漏依赖 RespVO 不定义 password/delFlag —— 没有 target 就不会被映射。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvert {

    /**
     * 明确忽略主键与审计字段，防止 ReqVO 伪造 id/createBy 注入
     */
    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemUserDO convert(UserCreateReqVO reqVO);

    SystemUserDO convert(UserUpdateReqVO reqVO);

    UserRespVO convert(SystemUserDO user);

    List<UserRespVO> convertList(List<SystemUserDO> users);

    /**
     * 分页结果转换：DO 分页 → VO 分页，全项目复用此模式
     */
    default PageResult<UserRespVO> convertPage(PageResult<SystemUserDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

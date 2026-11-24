package cn.lycoder.boot.module.system.convert.menu;

import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuRespVO;
import cn.lycoder.boot.module.system.controller.admin.menu.vo.MenuUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.menu.SystemMenuDO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 菜单对象转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuConvert {

    /**
     * 明确忽略主键与审计字段，防止 ReqVO 伪造 id/createBy 注入
     */
    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "menuId", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemMenuDO convert(MenuCreateReqVO reqVO);

    SystemMenuDO convert(MenuUpdateReqVO reqVO);

    MenuRespVO convert(SystemMenuDO menu);

    List<MenuRespVO> convertList(List<SystemMenuDO> menus);
}

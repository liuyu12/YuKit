package cn.lycoder.boot.module.system.convert.notice;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeRespVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.notice.SystemNoticeDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 公告转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoticeConvert {

    /**
     * 创建：忽略主键与审计字段，防止 ReqVO 伪造注入
     */
    @Mapping(target = "noticeId", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemNoticeDO convert(NoticeCreateReqVO reqVO);

    /**
     * 更新：保留 noticeId（由路径参数覆盖后随体传入），忽略审计字段
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemNoticeDO convert(NoticeUpdateReqVO reqVO);

    NoticeRespVO convert(SystemNoticeDO notice);

    List<NoticeRespVO> convertList(List<SystemNoticeDO> notices);

    default PageResult<NoticeRespVO> convertPage(PageResult<SystemNoticeDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

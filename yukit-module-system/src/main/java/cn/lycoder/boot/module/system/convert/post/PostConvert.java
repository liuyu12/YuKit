package cn.lycoder.boot.module.system.convert.post;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostRespVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.post.SystemPostDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 岗位转换器（MapStruct 编译期生成实现）
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostConvert {

    /**
     * 创建：忽略主键与审计字段，防止 ReqVO 伪造注入
     */
    @Mapping(target = "postId", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemPostDO convert(PostCreateReqVO reqVO);

    /**
     * 更新：保留 postId（路径参数覆盖后随体传入），忽略审计字段
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SystemPostDO convert(PostUpdateReqVO reqVO);

    PostRespVO convert(SystemPostDO post);

    List<PostRespVO> convertList(List<SystemPostDO> posts);

    default PageResult<PostRespVO> convertPage(PageResult<SystemPostDO> page) {
        return PageResult.convert(page, this::convert);
    }
}

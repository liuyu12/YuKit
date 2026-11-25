package cn.lycoder.boot.module.system.service.post;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.post.SystemPostDO;

import java.util.List;

/**
 * 岗位管理 Service
 * <p>
 * 编码/名称全局唯一（编码另有 DB 唯一索引兜底并发）；更新校验唯一性时排除自身。
 */
public interface PostService {

    /**
     * 创建岗位
     *
     * @return 岗位ID
     */
    Long createPost(PostCreateReqVO reqVO);

    /**
     * 更新岗位（可改编码，排除自身查重）
     */
    void updatePost(PostUpdateReqVO reqVO);

    /**
     * 删除岗位
     * <p>
     * 已知边界：用户-岗位关联（sys_user_post）未建，无法校验"岗位正在被用户使用"
     */
    void deletePost(Long postId);

    /**
     * 按 ID 查询岗位 DO（不存在返回 null，存在性由调用方校验）
     */
    SystemPostDO getPost(Long postId);

    /**
     * 分页查询岗位
     */
    PageResult<SystemPostDO> getPostPage(PostPageReqVO reqVO);

    /**
     * 正常状态岗位列表（前端下拉选项数据源）
     */
    List<SystemPostDO> getSimplePostList();
}

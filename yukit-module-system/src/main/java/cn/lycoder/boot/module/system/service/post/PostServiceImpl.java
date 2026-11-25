package cn.lycoder.boot.module.system.service.post;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostUpdateReqVO;
import cn.lycoder.boot.module.system.convert.post.PostConvert;
import cn.lycoder.boot.module.system.dal.dataobject.post.SystemPostDO;
import cn.lycoder.boot.module.system.dal.mapper.post.SystemPostMapper;
import cn.lycoder.boot.module.system.dal.mapper.userpost.SystemUserPostMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.POST_CODE_DUPLICATE;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.POST_IN_USE;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.POST_NAME_DUPLICATE;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.POST_NOT_EXISTS;

/**
 * 岗位管理 Service 实现
 * <p>
 * 岗位量级小（十几条封顶）且写极少，不引入缓存；下拉数据源直接查库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final SystemPostMapper postMapper;
    private final PostConvert postConvert;
    private final SystemUserPostMapper userPostMapper;

    @Override
    public Long createPost(PostCreateReqVO reqVO) {
        validateCodeAndNameUnique(reqVO.getPostCode(), reqVO.getPostName(), null);
        SystemPostDO post = postConvert.convert(reqVO);
        post.setStatus(StringUtils.hasText(reqVO.getStatus())
                ? reqVO.getStatus() : SystemPostDO.STATUS_NORMAL);
        postMapper.insert(post);
        return post.getPostId();
    }

    @Override
    public void updatePost(PostUpdateReqVO reqVO) {
        validatePostExists(reqVO.getPostId());
        // 编码允许修改，查重排除自身
        validateCodeAndNameUnique(reqVO.getPostCode(), reqVO.getPostName(), reqVO.getPostId());
        postMapper.updateById(postConvert.convert(reqVO));
    }

    @Override
    public void deletePost(Long postId) {
        validatePostExists(postId);
        // 引用校验：岗位仍被未删除用户使用时拒绝删除（逻辑删除的用户不计入，不长期占用）
        if (userPostMapper.countActiveByPostId(postId) > 0) {
            throw new ServiceException(POST_IN_USE);
        }
        postMapper.deleteById(postId);
    }

    @Override
    public SystemPostDO getPost(Long postId) {
        return postMapper.selectById(postId);
    }

    @Override
    public PageResult<SystemPostDO> getPostPage(PostPageReqVO reqVO) {
        LambdaQueryWrapper<SystemPostDO> wrapper = Wrappers.<SystemPostDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getPostCode()), SystemPostDO::getPostCode, reqVO.getPostCode())
                .like(StringUtils.hasText(reqVO.getPostName()), SystemPostDO::getPostName, reqVO.getPostName())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemPostDO::getStatus, reqVO.getStatus())
                .orderByAsc(SystemPostDO::getPostSort)
                .orderByAsc(SystemPostDO::getPostId);
        Page<SystemPostDO> page = postMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<SystemPostDO> getSimplePostList() {
        return postMapper.selectList(Wrappers.<SystemPostDO>lambdaQuery()
                .eq(SystemPostDO::getStatus, SystemPostDO.STATUS_NORMAL)
                .orderByAsc(SystemPostDO::getPostSort)
                .orderByAsc(SystemPostDO::getPostId));
    }

    /**
     * 编码/名称全局唯一校验；excludePostId 非空时排除自身（更新场景）。
     * DB 唯一索引 uk_sys_post_code 兜底并发窗口的编码重复。
     */
    private void validateCodeAndNameUnique(String postCode, String postName, Long excludePostId) {
        Long codeCount = postMapper.selectCount(Wrappers.<SystemPostDO>lambdaQuery()
                .eq(SystemPostDO::getPostCode, postCode)
                .ne(excludePostId != null, SystemPostDO::getPostId, excludePostId));
        if (codeCount > 0) {
            throw new ServiceException(POST_CODE_DUPLICATE);
        }
        Long nameCount = postMapper.selectCount(Wrappers.<SystemPostDO>lambdaQuery()
                .eq(SystemPostDO::getPostName, postName)
                .ne(excludePostId != null, SystemPostDO::getPostId, excludePostId));
        if (nameCount > 0) {
            throw new ServiceException(POST_NAME_DUPLICATE);
        }
    }

    private void validatePostExists(Long postId) {
        if (postMapper.selectById(postId) == null) {
            throw new ServiceException(POST_NOT_EXISTS);
        }
    }
}

package cn.lycoder.boot.module.system.service.post;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostUpdateReqVO;
import cn.lycoder.boot.module.system.convert.post.PostConvertImpl;
import cn.lycoder.boot.module.system.dal.dataobject.post.SystemPostDO;
import cn.lycoder.boot.module.system.dal.mapper.post.SystemPostMapper;
import cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PostServiceImpl 单测：纯 Mockito，不起 Spring 上下文
 * <p>
 * Convert 使用 MapStruct 编译期生成的真实实现；
 * 两次 selectCount（编码/名称）按调用次序打桩，重名场景名下一条即断言终止。
 */
@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private SystemPostMapper postMapper;

    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(postMapper, new PostConvertImpl());
    }

    private PostCreateReqVO buildCreateReq(String code, String name) {
        PostCreateReqVO reqVO = new PostCreateReqVO();
        reqVO.setPostCode(code);
        reqVO.setPostName(name);
        reqVO.setPostCategory("tech");
        reqVO.setPostSort(5);
        return reqVO;
    }

    private void stubCounts(Long codeCount, Long nameCount) {
        when(postMapper.selectCount(any(Wrapper.class))).thenReturn(codeCount, nameCount);
    }

    @Test
    @DisplayName("创建：编码名称均未占用则成功，状态缺省补 0")
    void createShouldDefaultStatusNormalAndInsert() {
        stubCounts(0L, 0L);
        when(postMapper.insert(any(SystemPostDO.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, SystemPostDO.class).setPostId(9L);
            return 1;
        });

        Long postId = postService.createPost(buildCreateReq("dev", "开发工程师"));

        assertThat(postId).isEqualTo(9L);
        ArgumentCaptor<SystemPostDO> captor = ArgumentCaptor.forClass(SystemPostDO.class);
        verify(postMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SystemPostDO.STATUS_NORMAL);
        assertThat(captor.getValue().getPostCode()).isEqualTo("dev");
    }

    @Test
    @DisplayName("创建：编码重复即拒绝，不落库")
    void createShouldRejectDuplicateCode() {
        stubCounts(1L, 0L);

        assertThatThrownBy(() -> postService.createPost(buildCreateReq("ceo", "新董事长")))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.POST_CODE_DUPLICATE.getCode());
        verify(postMapper, never()).insert(any(SystemPostDO.class));
    }

    @Test
    @DisplayName("创建：名称重复拒绝（编码校验已通过）")
    void createShouldRejectDuplicateName() {
        stubCounts(0L, 1L);

        assertThatThrownBy(() -> postService.createPost(buildCreateReq("newcode", "项目经理")))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.POST_NAME_DUPLICATE.getCode());
    }

    @Test
    @DisplayName("更新：岗位不存在抛异常，不做查重")
    void updateShouldRejectMissingPost() {
        when(postMapper.selectById(404L)).thenReturn(null);
        PostUpdateReqVO reqVO = new PostUpdateReqVO();
        reqVO.setPostId(404L);
        reqVO.setPostCode("ghost");
        reqVO.setPostName("幽灵岗位");

        assertThatThrownBy(() -> postService.updatePost(reqVO))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.POST_NOT_EXISTS.getCode());
        verify(postMapper, never()).selectCount(any(Wrapper.class));
    }

    @Test
    @DisplayName("更新：自身占用同名不视为冲突（两次查重均排除自身后为 0）")
    void updateShouldAllowKeepingOwnCodeAndName() {
        when(postMapper.selectById(2L)).thenReturn(new SystemPostDO());
        stubCounts(0L, 0L);

        PostUpdateReqVO reqVO = new PostUpdateReqVO();
        reqVO.setPostId(2L);
        reqVO.setPostCode("se");
        reqVO.setPostName("项目经理");
        reqVO.setPostSort(3);
        postService.updatePost(reqVO);

        ArgumentCaptor<SystemPostDO> captor = ArgumentCaptor.forClass(SystemPostDO.class);
        verify(postMapper).updateById(captor.capture());
        assertThat(captor.getValue().getPostId()).isEqualTo(2L);
        assertThat(captor.getValue().getPostSort()).isEqualTo(3);
        verify(postMapper, times(2)).selectCount(any(Wrapper.class));
    }

    @Test
    @DisplayName("更新：编码被他人占用拒绝")
    void updateShouldRejectCodeOwnedByAnother() {
        when(postMapper.selectById(3L)).thenReturn(new SystemPostDO());
        stubCounts(1L, 0L);

        PostUpdateReqVO reqVO = new PostUpdateReqVO();
        reqVO.setPostId(3L);
        reqVO.setPostCode("ceo");
        reqVO.setPostName("改名的HR");

        assertThatThrownBy(() -> postService.updatePost(reqVO))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.POST_CODE_DUPLICATE.getCode());
        verify(postMapper, never()).updateById(any(SystemPostDO.class));
    }

    @Test
    @DisplayName("删除：岗位不存在抛异常")
    void deleteShouldRejectMissingPost() {
        when(postMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> postService.deletePost(99L))
                .isInstanceOf(ServiceException.class);
        verify(postMapper, never()).deleteById(99L);
    }
}

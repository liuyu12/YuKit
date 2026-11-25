package cn.lycoder.boot.module.system.controller.admin.post;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.framework.protection.idempotent.Idempotent;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostRespVO;
import cn.lycoder.boot.module.system.controller.admin.post.vo.PostUpdateReqVO;
import cn.lycoder.boot.module.system.convert.post.PostConvert;
import cn.lycoder.boot.module.system.dal.dataobject.post.SystemPostDO;
import cn.lycoder.boot.module.system.service.post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 岗位管理
 * <p>
 * RESTful 风格 + Sa-Token 细粒度权限，权限码与 V2 种子菜单 104 / 按钮 1020~1023 对应；
 * simple-list 为下拉数据源，复用列表权限（有列表权即可见选项）。
 */
@Tag(name = "岗位管理")
@RestController
@RequestMapping("/system/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostConvert postConvert;

    /**
     * 创建岗位
     */
    @Operation(summary = "创建岗位")
    @OperationLog(module = "岗位管理", name = "创建岗位", type = OperationType.INSERT)
    @Idempotent(timeout = 3, message = "请勿重复提交")
    @PostMapping
    @SaCheckPermission("system:post:add")
    public Result<Long> createPost(@RequestBody @Valid PostCreateReqVO reqVO) {
        return Result.success(postService.createPost(reqVO));
    }

    /**
     * 更新岗位
     */
    @Operation(summary = "更新岗位")
    @OperationLog(module = "岗位管理", name = "更新岗位", type = OperationType.UPDATE)
    @PutMapping
    @SaCheckPermission("system:post:edit")
    public Result<Boolean> updatePost(@RequestBody @Valid PostUpdateReqVO reqVO) {
        postService.updatePost(reqVO);
        return Result.success(true);
    }

    /**
     * 删除岗位
     */
    @Operation(summary = "删除岗位")
    @OperationLog(module = "岗位管理", name = "删除岗位", type = OperationType.DELETE)
    @DeleteMapping("/{postId}")
    @SaCheckPermission("system:post:remove")
    public Result<Boolean> deletePost(
            @Parameter(description = "岗位ID") @PathVariable("postId") Long postId) {
        postService.deletePost(postId);
        return Result.success(true);
    }

    /**
     * 查询岗位详情
     */
    @Operation(summary = "查询岗位详情")
    @GetMapping("/{postId}")
    @SaCheckPermission("system:post:query")
    public Result<PostRespVO> getPost(
            @Parameter(description = "岗位ID") @PathVariable("postId") Long postId) {
        SystemPostDO post = postService.getPost(postId);
        return Result.success(post == null ? null : postConvert.convert(post));
    }

    /**
     * 分页查询岗位
     */
    @Operation(summary = "分页查询岗位")
    @GetMapping("/page")
    @SaCheckPermission("system:post:list")
    public Result<PageResult<PostRespVO>> getPostPage(@Valid PostPageReqVO reqVO) {
        return Result.success(postConvert.convertPage(postService.getPostPage(reqVO)));
    }

    /**
     * 正常状态岗位简单列表（下拉选项数据源）
     */
    @Operation(summary = "获取岗位下拉列表")
    @GetMapping("/simple-list")
    @SaCheckPermission("system:post:list")
    public Result<List<PostRespVO>> getSimplePostList() {
        return Result.success(postConvert.convertList(postService.getSimplePostList()));
    }
}

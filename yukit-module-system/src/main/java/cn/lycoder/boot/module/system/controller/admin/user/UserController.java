package cn.lycoder.boot.module.system.controller.admin.user;

import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserResetPasswordReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserRespVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserUpdateReqVO;
import cn.lycoder.boot.module.system.convert.user.UserConvert;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.service.user.UserService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理
 * <p>
 * RESTful 风格：资源用复数名词，操作由 HTTP 方法表达，路径不再带 create/update/get 等动词
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/users")
@Validated // 激活方法参数直接标注的校验（@RequestParam/@PathVariable 单参数，抛 ConstraintViolationException）
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserConvert userConvert;

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户")
    @PostMapping
    @SaCheckPermission("system:user:create")
    public Result<Long> createUser(@RequestBody @Valid UserCreateReqVO reqVO) {
        return Result.success(userService.createUser(reqVO));
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户")
    @PutMapping("/{userId}")
    @SaCheckPermission("system:user:update")
    public Result<Boolean> updateUser(@Parameter(description = "用户ID") @PathVariable("userId") Long userId,
                                      @RequestBody @Valid UserUpdateReqVO reqVO) {
        // 以路径参数为准，避免 URL 与 body 不一致时误改
        reqVO.setUserId(userId);
        userService.updateUser(reqVO);
        return Result.success(true);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Operation(summary = "删除用户（逻辑删除）")
    @DeleteMapping("/{userId}")
    @SaCheckPermission("system:user:delete")
    public Result<Boolean> deleteUser(@Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return Result.success(true);
    }

    /**
     * 解锁用户登录（清除连续失败计数，解除锁定）
     */
    @Operation(summary = "解锁用户登录")
    @OperationLog(module = "用户管理", name = "解锁用户登录", type = OperationType.UPDATE)
    @PutMapping("/{userId}/unlock")
    @SaCheckPermission("monitor:logininfor:unlock")
    public Result<Boolean> unlockUser(@Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        userService.unlockUser(userId);
        return Result.success(true);
    }

    /**
     * 重置用户密码（留空取系统参数 sys.user.initPassword）
     * <p>
     * 权限码沿用 V2 种子 system:user:edit（RuoYi 惯例：重置密码归用户编辑）
     */
    @Operation(summary = "重置用户密码")
    @OperationLog(module = "用户管理", name = "重置用户密码", type = OperationType.UPDATE)
    @PutMapping("/{userId}/reset-password")
    @SaCheckPermission("system:user:edit")
    public Result<Boolean> resetPassword(@Parameter(description = "用户ID") @PathVariable("userId") Long userId,
                                         @RequestBody @Valid UserResetPasswordReqVO reqVO) {
        userService.resetPassword(userId, reqVO.getPassword());
        return Result.success(true);
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/{userId}")
    @SaCheckPermission("system:user:query")
    public Result<UserRespVO> getUser(@Parameter(description = "用户ID") @PathVariable("userId")
                                      @Positive(message = "用户ID必须为正整数") Long userId) {
        SystemUserDO user = userService.getUser(userId);
        if (user == null) {
            return Result.success(null);
        }
        UserRespVO respVO = userConvert.convert(user);
        // 详情回填关联岗位ID，供前端表单多选回显（列表查询不回填）
        respVO.setPostIds(userService.getUserPostIds(userId));
        return Result.success(respVO);
    }

    /**
     * 分页查询用户
     */
    @Operation(summary = "分页查询用户")
    @GetMapping
    @SaCheckPermission("system:user:query")
    public Result<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO reqVO) {
        PageResult<SystemUserDO> pageResult = userService.getUserPage(reqVO);
        return Result.success(userConvert.convertPage(pageResult));
    }
}

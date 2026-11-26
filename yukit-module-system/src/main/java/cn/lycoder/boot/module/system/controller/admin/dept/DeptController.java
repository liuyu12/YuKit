package cn.lycoder.boot.module.system.controller.admin.dept;

import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.framework.protection.idempotent.Idempotent;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptListReqVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptRespVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptUpdateReqVO;
import cn.lycoder.boot.module.system.convert.dept.DeptConvert;
import cn.lycoder.boot.module.system.service.dept.DeptService;
import cn.dev33.satoken.annotation.SaCheckPermission;
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
 * 部门管理
 * <p>
 * RESTful 风格 + Sa-Token 细粒度权限，权限码与 sys_menu.perms 对应
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/system/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;
    private final DeptConvert deptConvert;

    /**
     * 创建部门
     */
    @Operation(summary = "创建部门")
    @OperationLog(module = "部门管理", name = "创建部门", type = OperationType.INSERT)
    @Idempotent(timeout = 3, message = "请勿重复提交")
    @PostMapping
    @SaCheckPermission("system:dept:create")
    public Result<Long> createDept(@RequestBody @Valid DeptCreateReqVO reqVO) {
        return Result.success(deptService.createDept(reqVO));
    }

    /**
     * 更新部门
     */
    @Operation(summary = "更新部门")
    @OperationLog(module = "部门管理", name = "更新部门", type = OperationType.UPDATE)
    @PutMapping
    @SaCheckPermission("system:dept:update")
    public Result<Boolean> updateDept(@RequestBody @Valid DeptUpdateReqVO reqVO) {
        deptService.updateDept(reqVO);
        return Result.success(true);
    }

    /**
     * 删除部门
     */
    @Operation(summary = "删除部门")
    @OperationLog(module = "部门管理", name = "删除部门", type = OperationType.DELETE)
    @DeleteMapping("/{deptId}")
    @SaCheckPermission("system:dept:delete")
    public Result<Boolean> deleteDept(@Parameter(description = "部门ID", example = "100")
                                      @PathVariable Long deptId) {
        deptService.deleteDept(deptId);
        return Result.success(true);
    }

    /**
     * 获取部门详情
     */
    @Operation(summary = "获取部门详情")
    @GetMapping("/{deptId}")
    @SaCheckPermission("system:dept:query")
    public Result<DeptRespVO> getDept(@Parameter(description = "部门ID", example = "100")
                                      @PathVariable Long deptId) {
        return Result.success(deptConvert.convert(deptService.getDept(deptId)));
    }

    /**
     * 获取部门树
     */
    @Operation(summary = "获取部门树")
    @GetMapping("/tree")
    @SaCheckPermission("system:dept:query")
    public Result<List<DeptRespVO>> getDeptTree(DeptListReqVO reqVO) {
        return Result.success(deptService.getDeptTree(reqVO));
    }
}

package cn.lycoder.boot.module.system.controller.admin.config;

import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.framework.protection.idempotent.Idempotent;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigRespVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigUpdateReqVO;
import cn.lycoder.boot.module.system.convert.config.ConfigConvert;
import cn.lycoder.boot.module.system.dal.dataobject.config.SystemConfigDO;
import cn.lycoder.boot.module.system.service.config.ConfigService;
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

/**
 * 参数管理
 * <p>
 * RESTful 风格 + Sa-Token 细粒度权限，权限码与 V2 种子按钮 1030~1033 对应
 */
@Tag(name = "参数管理")
@RestController
@RequestMapping("/system/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;
    private final ConfigConvert configConvert;

    /**
     * 创建参数
     */
    @Operation(summary = "创建参数")
    @OperationLog(module = "参数管理", name = "创建参数", type = OperationType.INSERT)
    @Idempotent(timeout = 3, message = "请勿重复提交")
    @PostMapping
    @SaCheckPermission("system:config:add")
    public Result<Long> createConfig(@RequestBody @Valid ConfigCreateReqVO reqVO) {
        return Result.success(configService.createConfig(reqVO));
    }

    /**
     * 更新参数（键名与内置标记不可改）
     */
    @Operation(summary = "更新参数")
    @OperationLog(module = "参数管理", name = "更新参数", type = OperationType.UPDATE)
    @PutMapping("/{configId}")
    @SaCheckPermission("system:config:edit")
    public Result<Boolean> updateConfig(@Parameter(description = "参数主键") @PathVariable("configId") Long configId,
                                        @RequestBody @Valid ConfigUpdateReqVO reqVO) {
        reqVO.setConfigId(configId);
        configService.updateConfig(reqVO);
        return Result.success(true);
    }

    /**
     * 删除参数（内置参数不允许删除）
     */
    @Operation(summary = "删除参数")
    @OperationLog(module = "参数管理", name = "删除参数", type = OperationType.DELETE)
    @DeleteMapping("/{configId}")
    @SaCheckPermission("system:config:remove")
    public Result<Boolean> deleteConfig(@Parameter(description = "参数主键") @PathVariable("configId") Long configId) {
        configService.deleteConfig(configId);
        return Result.success(true);
    }

    /**
     * 获取参数详情
     */
    @Operation(summary = "获取参数详情")
    @GetMapping("/{configId}")
    @SaCheckPermission("system:config:query")
    public Result<ConfigRespVO> getConfig(@Parameter(description = "参数主键") @PathVariable("configId") Long configId) {
        SystemConfigDO config = configService.getConfig(configId);
        return Result.success(config == null ? null : configConvert.convert(config));
    }

    /**
     * 按键名查询参数值（业务侧读取入口，命中 config 缓存）
     */
    @Operation(summary = "按键名查询参数值")
    @GetMapping("/key/{configKey}")
    @SaCheckPermission("system:config:query")
    public Result<String> getConfigValueByKey(
            @Parameter(description = "参数键名") @PathVariable("configKey") String configKey) {
        return Result.success(configService.getConfigValue(configKey));
    }

    /**
     * 分页查询参数
     */
    @Operation(summary = "分页查询参数")
    @GetMapping
    @SaCheckPermission("system:config:list")
    public Result<PageResult<ConfigRespVO>> getConfigPage(@Valid ConfigPageReqVO reqVO) {
        PageResult<SystemConfigDO> pageResult = configService.getConfigPage(reqVO);
        return Result.success(configConvert.convertPage(pageResult));
    }

    /**
     * 刷新参数缓存（全量清空，下次读取回源重建）
     */
    @Operation(summary = "刷新参数缓存")
    @OperationLog(module = "参数管理", name = "刷新参数缓存", type = OperationType.UPDATE)
    @PostMapping("/refresh-cache")
    @SaCheckPermission("system:config:edit")
    public Result<Boolean> refreshCache() {
        configService.refreshCache();
        return Result.success(true);
    }
}

package cn.lycoder.boot.module.system.controller.admin.dict;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.framework.protection.idempotent.Idempotent;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataRespVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataUpdateReqVO;
import cn.lycoder.boot.module.system.convert.dict.DictDataConvert;
import cn.lycoder.boot.module.system.dal.dataobject.dict.SystemDictDataDO;
import cn.lycoder.boot.module.system.service.dict.DictDataService;
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
 * 字典数据管理
 * <p>
 * RESTful 风格 + Sa-Token 细粒度权限，权限码与 sys_menu.perms 对应
 */
@Tag(name = "字典数据管理")
@RestController
@RequestMapping("/system/dict-data")
@RequiredArgsConstructor
public class DictDataController {

    private final DictDataService dictDataService;
    private final DictDataConvert dictDataConvert;

    /**
     * 创建字典数据
     */
    @Operation(summary = "创建字典数据")
    @Idempotent(timeout = 3, message = "请勿重复提交")
    @PostMapping
    @SaCheckPermission("system:dict:create")
    public Result<Long> createDictData(@RequestBody @Valid DictDataCreateReqVO reqVO) {
        return Result.success(dictDataService.createDictData(reqVO));
    }

    /**
     * 更新字典数据
     */
    @Operation(summary = "更新字典数据")
    @PutMapping("/{dictCode}")
    @SaCheckPermission("system:dict:update")
    public Result<Boolean> updateDictData(@Parameter(description = "字典编码") @PathVariable("dictCode") Long dictCode,
                                          @RequestBody @Valid DictDataUpdateReqVO reqVO) {
        reqVO.setDictCode(dictCode);
        dictDataService.updateDictData(reqVO);
        return Result.success(true);
    }

    /**
     * 删除字典数据
     */
    @Operation(summary = "删除字典数据")
    @DeleteMapping("/{dictCode}")
    @SaCheckPermission("system:dict:delete")
    public Result<Boolean> deleteDictData(@Parameter(description = "字典编码") @PathVariable("dictCode") Long dictCode) {
        dictDataService.deleteDictData(dictCode);
        return Result.success(true);
    }

    /**
     * 获取字典数据详情
     */
    @Operation(summary = "获取字典数据详情")
    @GetMapping("/{dictCode}")
    @SaCheckPermission("system:dict:query")
    public Result<DictDataRespVO> getDictData(@Parameter(description = "字典编码") @PathVariable("dictCode") Long dictCode) {
        SystemDictDataDO dictData = dictDataService.getDictData(dictCode);
        return Result.success(dictData == null ? null : dictDataConvert.convert(dictData));
    }

    /**
     * 分页查询字典数据
     */
    @Operation(summary = "分页查询字典数据")
    @GetMapping
    @SaCheckPermission("system:dict:query")
    public Result<PageResult<DictDataRespVO>> getDictDataPage(@Valid DictDataPageReqVO reqVO) {
        PageResult<SystemDictDataDO> pageResult = dictDataService.getDictDataPage(reqVO);
        return Result.success(dictDataConvert.convertPage(pageResult));
    }

    /**
     * 按字典类型获取正常状态的数据列表（前端下拉/回显的高频只读，走缓存）
     */
    @Operation(summary = "按字典类型获取数据列表（前端下拉/回显，走缓存）")
    @GetMapping("/type/{dictType}")
    @SaCheckPermission("system:dict:query")
    public Result<List<DictDataRespVO>> getDictDataListByType(@Parameter(description = "字典类型标识") @PathVariable("dictType") String dictType) {
        return Result.success(dictDataService.getDictDataListByType(dictType));
    }
}

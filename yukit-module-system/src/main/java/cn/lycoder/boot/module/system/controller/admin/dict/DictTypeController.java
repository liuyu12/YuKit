package cn.lycoder.boot.module.system.controller.admin.dict;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeRespVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeUpdateReqVO;
import cn.lycoder.boot.module.system.convert.dict.DictTypeConvert;
import cn.lycoder.boot.module.system.dal.dataobject.dict.SystemDictTypeDO;
import cn.lycoder.boot.module.system.service.dict.DictTypeService;
import cn.dev33.satoken.annotation.SaCheckPermission;
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
 * 字典类型管理
 * <p>
 * RESTful 风格 + Sa-Token 细粒度权限，权限码与 sys_menu.perms 对应
 */
@RestController
@RequestMapping("/system/dict-types")
@RequiredArgsConstructor
public class DictTypeController {

    private final DictTypeService dictTypeService;
    private final DictTypeConvert dictTypeConvert;

    /**
     * 创建字典类型
     */
    @PostMapping
    @SaCheckPermission("system:dict:create")
    public Result<Long> createDictType(@RequestBody @Valid DictTypeCreateReqVO reqVO) {
        return Result.success(dictTypeService.createDictType(reqVO));
    }

    /**
     * 更新字典类型
     */
    @PutMapping("/{dictId}")
    @SaCheckPermission("system:dict:update")
    public Result<Boolean> updateDictType(@PathVariable("dictId") Long dictId,
                                          @RequestBody @Valid DictTypeUpdateReqVO reqVO) {
        reqVO.setDictId(dictId);
        dictTypeService.updateDictType(reqVO);
        return Result.success(true);
    }

    /**
     * 删除字典类型（该类型下存在字典数据时不允许删除）
     */
    @DeleteMapping("/{dictId}")
    @SaCheckPermission("system:dict:delete")
    public Result<Boolean> deleteDictType(@PathVariable("dictId") Long dictId) {
        dictTypeService.deleteDictType(dictId);
        return Result.success(true);
    }

    /**
     * 获取字典类型详情
     */
    @GetMapping("/{dictId}")
    @SaCheckPermission("system:dict:query")
    public Result<DictTypeRespVO> getDictType(@PathVariable("dictId") Long dictId) {
        SystemDictTypeDO dictType = dictTypeService.getDictType(dictId);
        return Result.success(dictType == null ? null : dictTypeConvert.convert(dictType));
    }

    /**
     * 分页查询字典类型
     */
    @GetMapping
    @SaCheckPermission("system:dict:query")
    public Result<PageResult<DictTypeRespVO>> getDictTypePage(@Valid DictTypePageReqVO reqVO) {
        PageResult<SystemDictTypeDO> pageResult = dictTypeService.getDictTypePage(reqVO);
        return Result.success(dictTypeConvert.convertPage(pageResult));
    }

    /**
     * 全部正常状态字典类型（下拉列表）
     */
    @GetMapping("/simple-list")
    @SaCheckPermission("system:dict:query")
    public Result<List<DictTypeRespVO>> getDictTypeSimpleList() {
        return Result.success(dictTypeConvert.convertList(dictTypeService.getDictTypeList()));
    }
}

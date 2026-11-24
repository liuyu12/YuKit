package cn.lycoder.boot.module.system.service.dict;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.dict.SystemDictTypeDO;

import java.util.List;

/**
 * 字典类型管理 Service 接口
 */
public interface DictTypeService {

    Long createDictType(DictTypeCreateReqVO reqVO);

    void updateDictType(DictTypeUpdateReqVO reqVO);

    void deleteDictType(Long dictId);

    SystemDictTypeDO getDictType(Long dictId);

    PageResult<SystemDictTypeDO> getDictTypePage(DictTypePageReqVO reqVO);

    /**
     * 全部正常状态字典类型（下拉列表）
     */
    List<SystemDictTypeDO> getDictTypeList();
}

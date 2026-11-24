package cn.lycoder.boot.module.system.service.dict;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataRespVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.dict.SystemDictDataDO;

import java.util.List;

/**
 * 字典数据管理 Service 接口
 */
public interface DictDataService {

    Long createDictData(DictDataCreateReqVO reqVO);

    void updateDictData(DictDataUpdateReqVO reqVO);

    void deleteDictData(Long dictCode);

    SystemDictDataDO getDictData(Long dictCode);

    PageResult<SystemDictDataDO> getDictDataPage(DictDataPageReqVO reqVO);

    /**
     * 按字典类型获取正常状态的数据列表（前端下拉/回显的高频只读，走缓存）
     *
     * @param dictType 字典类型标识
     * @return 字典数据列表
     */
    List<DictDataRespVO> getDictDataListByType(String dictType);
}

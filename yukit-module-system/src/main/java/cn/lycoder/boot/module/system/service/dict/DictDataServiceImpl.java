package cn.lycoder.boot.module.system.service.dict;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataRespVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictDataUpdateReqVO;
import cn.lycoder.boot.module.system.convert.dict.DictDataConvert;
import cn.lycoder.boot.module.system.dal.dataobject.dict.SystemDictDataDO;
import cn.lycoder.boot.module.system.dal.dataobject.dict.SystemDictTypeDO;
import cn.lycoder.boot.module.system.dal.mapper.dict.SystemDictDataMapper;
import cn.lycoder.boot.module.system.dal.mapper.dict.SystemDictTypeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.DICT_DATA_DUPLICATE;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.DICT_DATA_NOT_EXISTS;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.DICT_DATA_TYPE_NOT_EXISTS;

/**
 * 字典数据管理 Service 实现
 * <p>
 * 缓存：按 dictType 缓存正常状态数据列表（cacheNames=dict，规格见 application.yml）。
 * 字典数据变更频率极低、读取极高频，任何写操作全量清空 dict 缓存，保证一致性。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictDataServiceImpl implements DictDataService {

    private final SystemDictDataMapper dictDataMapper;
    private final SystemDictTypeMapper dictTypeMapper;
    private final DictDataConvert dictDataConvert;

    @Override
    @CacheEvict(cacheNames = "dict", allEntries = true)
    public Long createDictData(DictDataCreateReqVO reqVO) {
        // 1. 校验所属字典类型存在
        validateDictTypeExists(reqVO.getDictType());
        // 2. 同一类型下键值唯一
        validateDictValueUnique(reqVO.getDictType(), reqVO.getDictValue(), null);
        SystemDictDataDO dictData = dictDataConvert.convert(reqVO);
        dictData.setDictSort(reqVO.getDictSort() == null ? 0 : reqVO.getDictSort());
        dictData.setIsDefault(StringUtils.hasText(reqVO.getIsDefault())
                ? reqVO.getIsDefault() : SystemDictDataDO.DEFAULT_NO);
        dictData.setStatus(StringUtils.hasText(reqVO.getStatus())
                ? reqVO.getStatus() : SystemDictTypeDO.STATUS_ENABLE);
        dictDataMapper.insert(dictData);
        return dictData.getDictCode();
    }

    @Override
    @CacheEvict(cacheNames = "dict", allEntries = true)
    public void updateDictData(DictDataUpdateReqVO reqVO) {
        validateDictDataExists(reqVO.getDictCode());
        validateDictValueUnique(reqVO.getDictType(), reqVO.getDictValue(), reqVO.getDictCode());
        dictDataMapper.updateById(dictDataConvert.convert(reqVO));
    }

    @Override
    @CacheEvict(cacheNames = "dict", allEntries = true)
    public void deleteDictData(Long dictCode) {
        validateDictDataExists(dictCode);
        dictDataMapper.deleteById(dictCode);
    }

    @Override
    public SystemDictDataDO getDictData(Long dictCode) {
        return dictDataMapper.selectById(dictCode);
    }

    @Override
    public PageResult<SystemDictDataDO> getDictDataPage(DictDataPageReqVO reqVO) {
        LambdaQueryWrapper<SystemDictDataDO> wrapper = Wrappers.<SystemDictDataDO>lambdaQuery()
                .eq(StringUtils.hasText(reqVO.getDictType()), SystemDictDataDO::getDictType, reqVO.getDictType())
                .like(StringUtils.hasText(reqVO.getDictLabel()), SystemDictDataDO::getDictLabel, reqVO.getDictLabel())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemDictDataDO::getStatus, reqVO.getStatus())
                .orderByAsc(SystemDictDataDO::getDictSort)
                .orderByAsc(SystemDictDataDO::getDictCode);
        Page<SystemDictDataDO> page = dictDataMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Cacheable(cacheNames = "dict", key = "#dictType")
    public List<DictDataRespVO> getDictDataListByType(String dictType) {
        List<SystemDictDataDO> list = dictDataMapper.selectList(Wrappers.<SystemDictDataDO>lambdaQuery()
                .eq(SystemDictDataDO::getDictType, dictType)
                .eq(SystemDictDataDO::getStatus, SystemDictTypeDO.STATUS_ENABLE)
                .orderByAsc(SystemDictDataDO::getDictSort)
                .orderByAsc(SystemDictDataDO::getDictCode));
        return dictDataConvert.convertList(list);
    }

    private void validateDictDataExists(Long dictCode) {
        if (dictDataMapper.selectById(dictCode) == null) {
            throw new ServiceException(DICT_DATA_NOT_EXISTS);
        }
    }

    private void validateDictTypeExists(String dictType) {
        Long count = dictTypeMapper.selectCount(Wrappers.<SystemDictTypeDO>lambdaQuery()
                .eq(SystemDictTypeDO::getDictType, dictType));
        if (count == null || count == 0) {
            throw new ServiceException(DICT_DATA_TYPE_NOT_EXISTS);
        }
    }

    private void validateDictValueUnique(String dictType, String dictValue, Long excludeCode) {
        Long count = dictDataMapper.selectCount(Wrappers.<SystemDictDataDO>lambdaQuery()
                .eq(SystemDictDataDO::getDictType, dictType)
                .eq(SystemDictDataDO::getDictValue, dictValue)
                .ne(excludeCode != null, SystemDictDataDO::getDictCode, excludeCode));
        if (count != null && count > 0) {
            throw new ServiceException(DICT_DATA_DUPLICATE);
        }
    }
}

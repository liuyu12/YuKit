package cn.lycoder.boot.module.system.service.dict;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.dict.vo.DictTypeUpdateReqVO;
import cn.lycoder.boot.module.system.convert.dict.DictTypeConvert;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.DICT_TYPE_DUPLICATE;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.DICT_TYPE_HAS_DATA;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.DICT_TYPE_NOT_EXISTS;

/**
 * 字典类型管理 Service 实现
 * <p>
 * 缓存：字典类型与字典数据共享同一份 dict 缓存（以 dictType 为键）。
 * 类型重命名会级联更新其下所有数据的 dict_type，故写操作全量清空 dict 缓存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl implements DictTypeService {

    private final SystemDictTypeMapper dictTypeMapper;
    private final SystemDictDataMapper dictDataMapper;
    private final DictTypeConvert dictTypeConvert;

    @Override
    public Long createDictType(DictTypeCreateReqVO reqVO) {
        validateDictTypeUnique(reqVO.getDictType(), null);
        SystemDictTypeDO dictType = dictTypeConvert.convert(reqVO);
        dictType.setStatus(StringUtils.hasText(reqVO.getStatus())
                ? reqVO.getStatus() : SystemDictTypeDO.STATUS_ENABLE);
        dictTypeMapper.insert(dictType);
        return dictType.getDictId();
    }

    @Override
    @CacheEvict(cacheNames = "dict", allEntries = true)
    public void updateDictType(DictTypeUpdateReqVO reqVO) {
        SystemDictTypeDO existing = validateDictTypeExists(reqVO.getDictId());
        validateDictTypeUnique(reqVO.getDictType(), reqVO.getDictId());

        dictTypeMapper.updateById(dictTypeConvert.convert(reqVO));

        // 类型标识变更：级联更新其下字典数据的 dict_type，否则数据会与旧类型失联、缓存键失效
        String oldType = existing.getDictType();
        if (!oldType.equals(reqVO.getDictType())) {
            SystemDictDataDO patch = new SystemDictDataDO();
            patch.setDictType(reqVO.getDictType());
            dictDataMapper.update(patch, Wrappers.<SystemDictDataDO>lambdaUpdate()
                    .eq(SystemDictDataDO::getDictType, oldType));
        }
    }

    @Override
    @CacheEvict(cacheNames = "dict", allEntries = true)
    public void deleteDictType(Long dictId) {
        SystemDictTypeDO existing = validateDictTypeExists(dictId);
        // 存在字典数据则拒绝删除，避免产生孤儿数据
        Long dataCount = dictDataMapper.selectCount(Wrappers.<SystemDictDataDO>lambdaQuery()
                .eq(SystemDictDataDO::getDictType, existing.getDictType()));
        if (dataCount != null && dataCount > 0) {
            throw new ServiceException(DICT_TYPE_HAS_DATA);
        }
        dictTypeMapper.deleteById(dictId);
    }

    @Override
    public SystemDictTypeDO getDictType(Long dictId) {
        return dictTypeMapper.selectById(dictId);
    }

    @Override
    public PageResult<SystemDictTypeDO> getDictTypePage(DictTypePageReqVO reqVO) {
        LambdaQueryWrapper<SystemDictTypeDO> wrapper = Wrappers.<SystemDictTypeDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getDictName()), SystemDictTypeDO::getDictName, reqVO.getDictName())
                .like(StringUtils.hasText(reqVO.getDictType()), SystemDictTypeDO::getDictType, reqVO.getDictType())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemDictTypeDO::getStatus, reqVO.getStatus())
                .orderByAsc(SystemDictTypeDO::getDictId);
        Page<SystemDictTypeDO> page = dictTypeMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<SystemDictTypeDO> getDictTypeList() {
        return dictTypeMapper.selectList(Wrappers.<SystemDictTypeDO>lambdaQuery()
                .eq(SystemDictTypeDO::getStatus, SystemDictTypeDO.STATUS_ENABLE)
                .orderByAsc(SystemDictTypeDO::getDictId));
    }

    private SystemDictTypeDO validateDictTypeExists(Long dictId) {
        SystemDictTypeDO dictType = dictTypeMapper.selectById(dictId);
        if (dictType == null) {
            throw new ServiceException(DICT_TYPE_NOT_EXISTS);
        }
        return dictType;
    }

    private void validateDictTypeUnique(String dictType, Long excludeId) {
        Long count = dictTypeMapper.selectCount(Wrappers.<SystemDictTypeDO>lambdaQuery()
                .eq(SystemDictTypeDO::getDictType, dictType)
                .ne(excludeId != null, SystemDictTypeDO::getDictId, excludeId));
        if (count != null && count > 0) {
            throw new ServiceException(DICT_TYPE_DUPLICATE);
        }
    }
}

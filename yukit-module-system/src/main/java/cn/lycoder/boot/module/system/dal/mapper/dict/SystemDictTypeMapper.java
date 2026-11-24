package cn.lycoder.boot.module.system.dal.mapper.dict;

import cn.lycoder.boot.module.system.dal.dataobject.dict.SystemDictTypeDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 字典类型 Mapper
 * <p>
 * 均为单表操作，直接使用 MyBatis-Plus 的 BaseMapper + LambdaQueryWrapper，无需 XML
 */
public interface SystemDictTypeMapper extends BaseMapper<SystemDictTypeDO> {
}

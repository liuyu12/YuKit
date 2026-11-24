package cn.lycoder.boot.module.system.dal.mapper.operatelog;

import cn.lycoder.boot.module.system.dal.dataobject.operatelog.SystemOperateLogDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 操作日志 Mapper
 * <p>
 * 单表读写，直接使用 MyBatis-Plus 的 BaseMapper + LambdaQueryWrapper，无需 XML
 */
public interface SystemOperateLogMapper extends BaseMapper<SystemOperateLogDO> {
}

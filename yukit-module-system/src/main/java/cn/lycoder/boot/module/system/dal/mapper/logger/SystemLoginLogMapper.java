package cn.lycoder.boot.module.system.dal.mapper.logger;

import cn.lycoder.boot.module.system.dal.dataobject.logger.SystemLoginLogDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 登录日志 Mapper
 * <p>
 * 单表读写，直接使用 MyBatis-Plus 的 BaseMapper + LambdaQueryWrapper，无需 XML
 */
public interface SystemLoginLogMapper extends BaseMapper<SystemLoginLogDO> {
}

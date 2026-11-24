package cn.lycoder.boot.module.system.service.logger;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.logger.vo.LoginLogPageReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.logger.SystemLoginLogDO;
import cn.lycoder.boot.module.system.dal.mapper.logger.SystemLoginLogMapper;
import cn.lycoder.boot.module.system.enums.logger.LoginResultTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 登录日志 Service 实现
 */
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    /** 列宽上限，与 V3 建表字段长度对齐，超长截断避免数据截断异常 */
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_MSG_LENGTH = 255;
    private static final int MAX_IP_LENGTH = 128;
    private static final int MAX_USER_AGENT_LENGTH = 512;

    private final SystemLoginLogMapper loginLogMapper;

    @Override
    public void createLoginLog(Long userId, String username, LoginResultTypeEnum result,
                               String msg, String userIp, String userAgent) {
        SystemLoginLogDO log = new SystemLoginLogDO();
        log.setUserId(userId);
        log.setUsername(truncate(username, MAX_USERNAME_LENGTH));
        log.setResult(result.getCode());
        log.setMsg(truncate(msg, MAX_MSG_LENGTH));
        log.setUserIp(truncate(userIp, MAX_IP_LENGTH));
        log.setUserAgent(truncate(userAgent, MAX_USER_AGENT_LENGTH));
        log.setCreateTime(LocalDateTime.now());
        loginLogMapper.insert(log);
    }

    @Override
    public PageResult<SystemLoginLogDO> getLoginLogPage(LoginLogPageReqVO reqVO) {
        LambdaQueryWrapper<SystemLoginLogDO> wrapper = Wrappers.<SystemLoginLogDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getUsername()), SystemLoginLogDO::getUsername, reqVO.getUsername())
                .eq(reqVO.getResult() != null, SystemLoginLogDO::getResult, reqVO.getResult())
                .like(StringUtils.hasText(reqVO.getUserIp()), SystemLoginLogDO::getUserIp, reqVO.getUserIp())
                .ge(reqVO.getBeginTime() != null, SystemLoginLogDO::getCreateTime, reqVO.getBeginTime())
                .le(reqVO.getEndTime() != null, SystemLoginLogDO::getCreateTime, reqVO.getEndTime())
                .orderByDesc(SystemLoginLogDO::getId);
        Page<SystemLoginLogDO> page = loginLogMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public SystemLoginLogDO getLoginLog(Long id) {
        return loginLogMapper.selectById(id);
    }

    @Override
    public void deleteLoginLog(Long id) {
        loginLogMapper.deleteById(id);
    }

    /**
     * 按列宽截断，避免超长 UA/账号触发数据截断异常
     */
    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

package cn.lycoder.boot.module.system.service.operatelog;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.operatelog.vo.OperateLogPageReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.operatelog.SystemOperateLogDO;
import cn.lycoder.boot.module.system.dal.mapper.operatelog.SystemOperateLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志 Service 实现
 */
@Service
@RequiredArgsConstructor
public class OperateLogServiceImpl implements OperateLogService {

    private final SystemOperateLogMapper operateLogMapper;

    @Override
    public PageResult<SystemOperateLogDO> getOperateLogPage(OperateLogPageReqVO reqVO) {
        LambdaQueryWrapper<SystemOperateLogDO> wrapper = Wrappers.<SystemOperateLogDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getModule()), SystemOperateLogDO::getModule, reqVO.getModule())
                .like(StringUtils.hasText(reqVO.getName()), SystemOperateLogDO::getName, reqVO.getName())
                .like(StringUtils.hasText(reqVO.getOperator()), SystemOperateLogDO::getOperator, reqVO.getOperator())
                .eq(reqVO.getType() != null, SystemOperateLogDO::getType, reqVO.getType())
                .eq(reqVO.getStatus() != null, SystemOperateLogDO::getStatus, reqVO.getStatus())
                .ge(reqVO.getBeginTime() != null, SystemOperateLogDO::getStartTime, reqVO.getBeginTime())
                .le(reqVO.getEndTime() != null, SystemOperateLogDO::getStartTime, reqVO.getEndTime())
                .orderByDesc(SystemOperateLogDO::getId);
        Page<SystemOperateLogDO> page = operateLogMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public SystemOperateLogDO getOperateLog(Long id) {
        return operateLogMapper.selectById(id);
    }

    @Override
    public void deleteOperateLog(Long id) {
        operateLogMapper.deleteById(id);
    }
}

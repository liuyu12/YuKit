package cn.lycoder.boot.module.system.service.logger;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.logger.vo.LoginLogPageReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.logger.SystemLoginLogDO;
import cn.lycoder.boot.module.system.enums.logger.LoginResultTypeEnum;

/**
 * 登录日志 Service
 * <p>
 * 记录由 {@code AuthServiceImpl} 在登录各分支显式写入，本接口同时提供查询与维护。
 */
public interface LoginLogService {

    /**
     * 记录一条登录日志
     *
     * @param userId   登录用户 ID，账号不存在时可为 null
     * @param username 登录账号
     * @param result   登录结果
     * @param msg      结果补充说明（失败原因 / 剩余尝试次数等）
     * @param userIp   客户端 IP
     * @param userAgent 浏览器 UA
     */
    void createLoginLog(Long userId, String username, LoginResultTypeEnum result,
                        String msg, String userIp, String userAgent);

    /**
     * 分页查询登录日志
     */
    PageResult<SystemLoginLogDO> getLoginLogPage(LoginLogPageReqVO reqVO);

    /**
     * 获取登录日志详情
     */
    SystemLoginLogDO getLoginLog(Long id);

    /**
     * 删除登录日志
     */
    void deleteLoginLog(Long id);
}

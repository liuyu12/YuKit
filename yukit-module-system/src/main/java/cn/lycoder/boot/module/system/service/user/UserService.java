package cn.lycoder.boot.module.system.service.user;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;

/**
 * 用户管理 Service 接口
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @return 用户ID
     */
    Long createUser(UserCreateReqVO reqVO);

    /**
     * 更新用户
     */
    void updateUser(UserUpdateReqVO reqVO);

    /**
     * 删除用户（逻辑删除）
     */
    void deleteUser(Long userId);

    /**
     * 强制解锁用户登录：清除失败计数，立即解除因连续失败触发的锁定
     */
    void unlockUser(Long userId);

    /**
     * 获取用户，不存在抛异常
     */
    SystemUserDO getUser(Long userId);

    /**
     * 分页查询用户
     */
    PageResult<SystemUserDO> getUserPage(UserPageReqVO reqVO);
}

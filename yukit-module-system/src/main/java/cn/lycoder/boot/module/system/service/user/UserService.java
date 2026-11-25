package cn.lycoder.boot.module.system.service.user;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.UserProfileUpdateReqVO;
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
     * 重置用户密码：指定则用新密码，留空取系统参数 sys.user.initPassword；
     * 同步清除登录失败计数，让用户可立即以新密码登录
     *
     * @param userId      用户ID
     * @param rawPassword 新密码明文，可为空
     */
    void resetPassword(Long userId, String rawPassword);

    /**
     * 获取用户，不存在抛异常
     */
    SystemUserDO getUser(Long userId);

    /**
     * 个人中心：获取指定用户的资料，用户不存在抛异常（会话对应用户已被删除的防御）
     */
    SystemUserDO getUserProfile(Long userId);

    /**
     * 个人中心：更新指定用户的自助资料（昵称/邮箱/手机/性别）
     */
    void updateUserProfile(Long userId, UserProfileUpdateReqVO reqVO);

    /**
     * 分页查询用户
     */
    PageResult<SystemUserDO> getUserPage(UserPageReqVO reqVO);
}

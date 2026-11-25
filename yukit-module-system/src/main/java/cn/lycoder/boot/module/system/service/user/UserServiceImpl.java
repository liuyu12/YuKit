package cn.lycoder.boot.module.system.service.user;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.mybatis.datapermission.annotation.IgnoreDataPermission;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.UserProfileUpdateReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserUpdateReqVO;
import cn.lycoder.boot.module.system.convert.user.UserConvert;
import cn.lycoder.boot.module.system.dal.dataobject.post.SystemPostDO;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.post.SystemPostMapper;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import cn.lycoder.boot.module.system.dal.mapper.userpost.SystemUserPostMapper;
import cn.lycoder.boot.module.system.service.auth.LoginAttemptService;
import cn.lycoder.boot.module.system.service.config.ConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.POST_NOT_EXISTS;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.USER_NAME_EXISTS;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 用户管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * 重置密码默认值来源参数（参数管理可运行期维护）
     */
    private static final String CONFIG_KEY_INIT_PASSWORD = "sys.user.initPassword";
    /**
     * 参数缺失/空值时的兜底初始密码，与 V6 种子默认值一致
     */
    private static final String FALLBACK_INIT_PASSWORD = "123456";

    private final SystemUserMapper userMapper;
    private final UserConvert userConvert;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final ConfigService configService;
    private final SystemUserPostMapper userPostMapper;
    private final SystemPostMapper postMapper;

    @Override
    @IgnoreDataPermission
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateReqVO reqVO) {
        // 账号唯一性校验须跨全表进行，不受当前用户可见部门限制，故忽略数据权限
        // 1. 校验账号唯一
        validateUserNameUnique(reqVO.getUserName(), null);
        // 2. 校验所选岗位均存在（防前端伪造/脏 ID 落库）
        validatePostsExist(reqVO.getPostIds());
        // 3. ReqVO → DO（MapStruct），密码单独加密
        SystemUserDO user = userConvert.convert(reqVO);
        user.setPassword(passwordEncoder.encode(reqVO.getPassword()));
        user.setUserType("00");
        user.setStatus(StringUtils.hasText(reqVO.getStatus())
                ? reqVO.getStatus() : SystemUserDO.STATUS_ENABLE);
        // 4. 插入，createBy/createTime 由 MetaObjectHandler 自动填充
        userMapper.insert(user);
        // 5. 保存用户-岗位关联（多对多，全量覆盖）
        saveUserPosts(user.getUserId(), reqVO.getPostIds());
        return user.getUserId();
    }

    @Override
    @IgnoreDataPermission
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateReqVO reqVO) {
        // 1. 校验用户存在
        validateUserExists(reqVO.getUserId());
        // 2. 校验所选岗位均存在
        validatePostsExist(reqVO.getPostIds());
        // 3. ReqVO → DO，password 不在更新 VO 中，避免误改
        SystemUserDO updateObj = userConvert.convert(reqVO);
        // updateBy/updateTime 由 MetaObjectHandler 自动填充
        userMapper.updateById(updateObj);
        // 4. 全量覆盖用户-岗位关联
        saveUserPosts(reqVO.getUserId(), reqVO.getPostIds());
    }

    @Override
    @IgnoreDataPermission
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        validateUserExists(userId);
        // @TableLogic 生效，实际执行 UPDATE del_flag='2'
        userMapper.deleteById(userId);
        // 物理清除岗位关联，避免逻辑删除的用户长期占用岗位（影响删岗引用校验）
        userPostMapper.deleteByUserId(userId);
    }

    @Override
    @IgnoreDataPermission
    public void unlockUser(Long userId) {
        // 解锁以用户名为维度清除 Redis 失败计数，先确保用户存在
        SystemUserDO user = validateUserExists(userId);
        loginAttemptService.reset(user.getUserName());
    }

    @Override
    @IgnoreDataPermission
    public void resetPassword(Long userId, String rawPassword) {
        SystemUserDO user = validateUserExists(userId);
        // 未指定则读系统参数（走 config 缓存，参数管理改值即时生效）；参数缺失再兜底内置默认
        String password = StringUtils.hasText(rawPassword)
                ? rawPassword : configService.getConfigValue(CONFIG_KEY_INIT_PASSWORD);
        if (!StringUtils.hasText(password)) {
            password = FALLBACK_INIT_PASSWORD;
        }
        // 只更新密码列（null 字段 updateById 自动跳过），updateBy/updateTime 由 MetaObjectHandler 填充
        SystemUserDO updateObj = new SystemUserDO();
        updateObj.setUserId(userId);
        updateObj.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(updateObj);
        // 凭证已变更，顺带清除失败计数解除锁定，用户可立即以新密码登录
        loginAttemptService.reset(user.getUserName());
    }

    @Override
    @IgnoreDataPermission
    public SystemUserDO getUser(Long userId) {
        // 详情按主键查询，交由权限码控制访问，数据范围仅在列表生效
        return userMapper.selectById(userId);
    }

    @Override
    @IgnoreDataPermission
    public SystemUserDO getUserProfile(Long userId) {
        // 自助场景必须能读到自己：自定义数据范围可能不含本部门，不能复用受拦截的 getUser
        return validateUserExists(userId);
    }

    @Override
    @IgnoreDataPermission
    public void updateUserProfile(Long userId, UserProfileUpdateReqVO reqVO) {
        validateUserExists(userId);
        // 只带四个自助字段（convert 产物其余列 null 被 updateById 跳过），主键显式设定防伪造
        SystemUserDO updateObj = userConvert.convert(reqVO);
        updateObj.setUserId(userId);
        userMapper.updateById(updateObj);
    }

    @Override
    @IgnoreDataPermission
    public void updateUserAvatar(Long userId, String avatar) {
        validateUserExists(userId);
        // 仅触碰 avatar 列；旧头像文件不自动删除（无引用计数，误删风险大于存储收益）
        SystemUserDO updateObj = new SystemUserDO();
        updateObj.setUserId(userId);
        updateObj.setAvatar(avatar);
        userMapper.updateById(updateObj);
    }

    @Override
    public PageResult<SystemUserDO> getUserPage(UserPageReqVO reqVO) {
        // 数据权限由 MP DataPermissionInterceptor 对 sys_user 自动追加 dept_id 条件，无需在此手动处理
        LambdaQueryWrapper<SystemUserDO> wrapper = Wrappers.<SystemUserDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getUserName()), SystemUserDO::getUserName, reqVO.getUserName())
                .like(StringUtils.hasText(reqVO.getPhonenumber()), SystemUserDO::getPhonenumber, reqVO.getPhonenumber())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemUserDO::getStatus, reqVO.getStatus())
                .orderByDesc(SystemUserDO::getUserId);
        Page<SystemUserDO> page = userMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    /**
     * 校验用户存在，不存在抛业务异常
     */
    private SystemUserDO validateUserExists(Long userId) {
        SystemUserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(USER_NOT_EXISTS);
        }
        return user;
    }

    /**
     * 校验账号唯一（excludeUserId 用于更新场景排除自身，当前更新不改账号，预留）
     */
    private void validateUserNameUnique(String userName, Long excludeUserId) {
        Long count = userMapper.selectCount(Wrappers.<SystemUserDO>lambdaQuery()
                .eq(SystemUserDO::getUserName, userName)
                .ne(excludeUserId != null, SystemUserDO::getUserId, excludeUserId));
        if (count != null && count > 0) {
            throw new ServiceException(USER_NAME_EXISTS);
        }
    }

    @Override
    @IgnoreDataPermission
    public List<Long> getUserPostIds(Long userId) {
        return userPostMapper.selectPostIdsByUserId(userId);
    }

    /**
     * 全量覆盖用户-岗位关联：先清空该用户全部关联，再按最新选择批量插入。
     * postIds 为空表示清空（用户可不属于任何岗位）。
     */
    private void saveUserPosts(Long userId, List<Long> postIds) {
        userPostMapper.deleteByUserId(userId);
        if (!CollectionUtils.isEmpty(postIds)) {
            userPostMapper.insertBatch(userId, postIds);
        }
    }

    /**
     * 校验所选岗位全部存在（防前端伪造/脏 ID 落库），空选择直接跳过。
     * 去重后比对命中数，任一无效 ID 即拒绝整笔操作。
     */
    private void validatePostsExist(List<Long> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return;
        }
        List<Long> distinctIds = postIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        List<SystemPostDO> existPosts = postMapper.selectBatchIds(distinctIds);
        if (existPosts == null || existPosts.size() < distinctIds.size()) {
            throw new ServiceException(POST_NOT_EXISTS);
        }
    }
}

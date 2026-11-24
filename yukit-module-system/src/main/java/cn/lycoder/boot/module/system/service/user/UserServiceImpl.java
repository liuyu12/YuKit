package cn.lycoder.boot.module.system.service.user;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.user.vo.UserUpdateReqVO;
import cn.lycoder.boot.module.system.convert.user.UserConvert;
import cn.lycoder.boot.module.system.dal.dataobject.user.SystemUserDO;
import cn.lycoder.boot.module.system.dal.mapper.user.SystemUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.USER_NAME_EXISTS;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 用户管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SystemUserMapper userMapper;
    private final UserConvert userConvert;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Long createUser(UserCreateReqVO reqVO) {
        // 1. 校验账号唯一
        validateUserNameUnique(reqVO.getUserName(), null);
        // 2. ReqVO → DO（MapStruct），密码单独加密
        SystemUserDO user = userConvert.convert(reqVO);
        user.setPassword(passwordEncoder.encode(reqVO.getPassword()));
        user.setUserType("00");
        user.setStatus(StringUtils.hasText(reqVO.getStatus())
                ? reqVO.getStatus() : SystemUserDO.STATUS_ENABLE);
        // 3. 插入，createBy/createTime 由 MetaObjectHandler 自动填充
        userMapper.insert(user);
        return user.getUserId();
    }

    @Override
    public void updateUser(UserUpdateReqVO reqVO) {
        // 1. 校验用户存在
        validateUserExists(reqVO.getUserId());
        // 2. ReqVO → DO，password 不在更新 VO 中，避免误改
        SystemUserDO updateObj = userConvert.convert(reqVO);
        // updateBy/updateTime 由 MetaObjectHandler 自动填充
        userMapper.updateById(updateObj);
    }

    @Override
    public void deleteUser(Long userId) {
        validateUserExists(userId);
        // @TableLogic 生效，实际执行 UPDATE del_flag='2'
        userMapper.deleteById(userId);
    }

    @Override
    public SystemUserDO getUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public PageResult<SystemUserDO> getUserPage(UserPageReqVO reqVO) {
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
}

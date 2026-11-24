package cn.lycoder.boot.module.system.service.role;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RolePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.role.vo.RoleUpdateReqVO;
import cn.lycoder.boot.module.system.convert.role.RoleConvert;
import cn.lycoder.boot.module.system.dal.dataobject.role.SystemRoleDO;
import cn.lycoder.boot.module.system.dal.mapper.role.SystemRoleMapper;
import cn.lycoder.boot.module.system.dal.mapper.rolemenu.SystemRoleMenuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.ROLE_KEY_EXISTS;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.ROLE_NOT_EXISTS;

/**
 * 角色管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    /**
     * 超级管理员角色ID，禁止删除/修改，防止把自己锁死
     */
    public static final Long SUPER_ADMIN_ROLE_ID = 1L;

    private final SystemRoleMapper roleMapper;
    private final SystemRoleMenuMapper roleMenuMapper;
    private final RoleConvert roleConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleCreateReqVO reqVO) {
        // 1. 校验权限字符唯一
        validateRoleKeyUnique(reqVO.getRoleKey(), null);
        // 2. ReqVO → DO
        SystemRoleDO role = roleConvert.convert(reqVO);
        role.setDataScope("1");
        role.setMenuCheckStrictly(true);
        role.setDeptCheckStrictly(true);
        role.setStatus(StringUtils.hasText(reqVO.getStatus())
                ? reqVO.getStatus() : SystemRoleDO.STATUS_ENABLE);
        roleMapper.insert(role);
        // 3. 创建时携带菜单则一并授权
        if (!CollectionUtils.isEmpty(reqVO.getMenuIds())) {
            roleMenuMapper.insertBatch(role.getRoleId(), reqVO.getMenuIds());
        }
        return role.getRoleId();
    }

    @Override
    public void updateRole(RoleUpdateReqVO reqVO) {
        SystemRoleDO role = validateRoleExists(reqVO.getRoleId());
        validateSuperAdmin(role);
        validateRoleKeyUnique(reqVO.getRoleKey(), reqVO.getRoleId());
        roleMapper.updateById(roleConvert.convert(reqVO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SystemRoleDO role = validateRoleExists(roleId);
        validateSuperAdmin(role);
        // 逻辑删除角色，同时清理授权关系，避免残留脏关联继续产生权限
        roleMapper.deleteById(roleId);
        roleMenuMapper.deleteByRoleId(roleId);
    }

    @Override
    public SystemRoleDO getRole(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public PageResult<SystemRoleDO> getRolePage(RolePageReqVO reqVO) {
        LambdaQueryWrapper<SystemRoleDO> wrapper = Wrappers.<SystemRoleDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getRoleName()), SystemRoleDO::getRoleName, reqVO.getRoleName())
                .like(StringUtils.hasText(reqVO.getRoleKey()), SystemRoleDO::getRoleKey, reqVO.getRoleKey())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemRoleDO::getStatus, reqVO.getStatus())
                .orderByAsc(SystemRoleDO::getRoleSort)
                .orderByAsc(SystemRoleDO::getRoleId);
        Page<SystemRoleDO> page = roleMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<SystemRoleDO> getRoleList() {
        return roleMapper.selectList(Wrappers.<SystemRoleDO>lambdaQuery()
                .eq(SystemRoleDO::getStatus, SystemRoleDO.STATUS_ENABLE)
                .orderByAsc(SystemRoleDO::getRoleSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenu(Long roleId, List<Long> menuIds) {
        validateRoleExists(roleId);
        // 全量覆盖：先删后插
        roleMenuMapper.deleteByRoleId(roleId);
        if (!CollectionUtils.isEmpty(menuIds)) {
            roleMenuMapper.insertBatch(roleId, menuIds);
        }
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    private SystemRoleDO validateRoleExists(Long roleId) {
        SystemRoleDO role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException(ROLE_NOT_EXISTS);
        }
        return role;
    }

    /**
     * 超级管理员角色保护
     */
    private void validateSuperAdmin(SystemRoleDO role) {
        if (SUPER_ADMIN_ROLE_ID.equals(role.getRoleId())) {
            throw new ServiceException(ROLE_NOT_EXISTS.getCode(), "超级管理员角色不允许操作");
        }
    }

    private void validateRoleKeyUnique(String roleKey, Long excludeRoleId) {
        Long count = roleMapper.selectCount(Wrappers.<SystemRoleDO>lambdaQuery()
                .eq(SystemRoleDO::getRoleKey, roleKey)
                .ne(excludeRoleId != null, SystemRoleDO::getRoleId, excludeRoleId));
        if (count != null && count > 0) {
            throw new ServiceException(ROLE_KEY_EXISTS);
        }
    }
}

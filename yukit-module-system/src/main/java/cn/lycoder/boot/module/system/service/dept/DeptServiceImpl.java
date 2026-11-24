package cn.lycoder.boot.module.system.service.dept;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptListReqVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptRespVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptUpdateReqVO;
import cn.lycoder.boot.module.system.convert.dept.DeptConvert;
import cn.lycoder.boot.module.system.dal.dataobject.dept.SystemDeptDO;
import cn.lycoder.boot.module.system.dal.mapper.dept.SystemDeptMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.*;

/**
 * 部门管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final SystemDeptMapper deptMapper;
    private final DeptConvert deptConvert;

    @Override
    public Long createDept(DeptCreateReqVO reqVO) {
        SystemDeptDO parent = validateParentDept(reqVO.getParentId(), null);
        validateDeptNameUnique(reqVO.getParentId(), reqVO.getDeptName(), null);
        SystemDeptDO dept = deptConvert.convert(reqVO);
        // ancestors = 父链 + 父 ID（顶级为 "0"），由服务端计算，不接受外部传入
        dept.setAncestors(buildAncestors(parent));
        dept.setStatus(StringUtils.hasText(reqVO.getStatus())
                ? reqVO.getStatus() : SystemDeptDO.STATUS_ENABLE);
        deptMapper.insert(dept);
        return dept.getDeptId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(DeptUpdateReqVO reqVO) {
        SystemDeptDO oldDept = validateDeptExists(reqVO.getDeptId());
        SystemDeptDO parent = validateParentDept(reqVO.getParentId(), reqVO.getDeptId());
        validateNotChild(reqVO.getDeptId(), reqVO.getParentId());
        validateDeptNameUnique(reqVO.getParentId(), reqVO.getDeptName(), reqVO.getDeptId());

        String newAncestors = buildAncestors(parent);
        SystemDeptDO updateObj = deptConvert.convert(reqVO);
        updateObj.setAncestors(newAncestors);
        deptMapper.updateById(updateObj);

        // 父链变化时级联刷新子孙 ancestors
        String oldAncestors = oldDept.getAncestors();
        if (newAncestors != null && !newAncestors.equals(oldAncestors)) {
            deptMapper.updateDeptChildren(oldAncestors, newAncestors, reqVO.getDeptId());
        }
    }

    @Override
    public void deleteDept(Long deptId) {
        validateDeptExists(deptId);
        Long children = deptMapper.selectCount(Wrappers.<SystemDeptDO>lambdaQuery()
                .eq(SystemDeptDO::getParentId, deptId));
        if (children != null && children > 0) {
            throw new ServiceException(DEPT_EXITS_CHILDREN);
        }
        deptMapper.deleteById(deptId);
    }

    @Override
    public SystemDeptDO getDept(Long deptId) {
        return deptMapper.selectById(deptId);
    }

    @Override
    public List<DeptRespVO> getDeptTree(DeptListReqVO reqVO) {
        List<SystemDeptDO> depts = deptMapper.selectList(Wrappers.<SystemDeptDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getDeptName()), SystemDeptDO::getDeptName, reqVO.getDeptName())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemDeptDO::getStatus, reqVO.getStatus())
                .orderByAsc(SystemDeptDO::getParentId)
                .orderByAsc(SystemDeptDO::getOrderNum));
        return buildTree(depts);
    }

    @Override
    public List<Long> getSelfAndChildDeptIds(Long deptId) {
        List<Long> ids = new ArrayList<>();
        if (deptId == null) {
            return ids;
        }
        ids.add(deptId);
        ids.addAll(deptMapper.selectChildDeptIds(deptId));
        return ids;
    }

    /**
     * 平铺列表 → 树；条件过滤后无根命中时退化为平铺返回（同菜单树策略）
     */
    private List<DeptRespVO> buildTree(List<SystemDeptDO> depts) {
        List<DeptRespVO> nodes = deptConvert.convertList(depts);
        Map<Long, List<DeptRespVO>> childrenMap = nodes.stream()
                .collect(Collectors.groupingBy(DeptRespVO::getParentId));
        List<DeptRespVO> roots = new ArrayList<>();
        for (DeptRespVO node : nodes) {
            node.setChildren(childrenMap.get(node.getDeptId()));
            if (SystemDeptDO.ROOT_PARENT_ID.equals(node.getParentId())) {
                roots.add(node);
            }
        }
        return roots.isEmpty() ? nodes : roots;
    }

    /**
     * 祖级链 = 父.ancestors + "," + 父.deptId；顶级父为 "0"
     */
    private String buildAncestors(SystemDeptDO parent) {
        if (parent == null) {
            return SystemDeptDO.ROOT_ANCESTORS;
        }
        return parent.getAncestors() + "," + parent.getDeptId();
    }

    private SystemDeptDO validateDeptExists(Long deptId) {
        SystemDeptDO dept = deptMapper.selectById(deptId);
        if (dept == null) {
            throw new ServiceException(DEPT_NOT_EXISTS);
        }
        return dept;
    }

    /**
     * 校验父部门合法，返回父部门 DO（顶级返回 null）
     */
    private SystemDeptDO validateParentDept(Long parentId, Long currentDeptId) {
        if (parentId == null || SystemDeptDO.ROOT_PARENT_ID.equals(parentId)) {
            return null;
        }
        if (parentId.equals(currentDeptId)) {
            throw new ServiceException(DEPT_PARENT_ERROR);
        }
        SystemDeptDO parent = deptMapper.selectById(parentId);
        if (parent == null) {
            throw new ServiceException(DEPT_PARENT_NOT_EXISTS);
        }
        return parent;
    }

    /**
     * 新父部门不能是自己的子孙，否则形成环
     */
    private void validateNotChild(Long deptId, Long newParentId) {
        if (newParentId == null || SystemDeptDO.ROOT_PARENT_ID.equals(newParentId)) {
            return;
        }
        List<Long> childIds = deptMapper.selectChildDeptIds(deptId);
        if (childIds.contains(newParentId)) {
            throw new ServiceException(DEPT_PARENT_IS_CHILD);
        }
    }

    private void validateDeptNameUnique(Long parentId, String deptName, Long excludeDeptId) {
        Long count = deptMapper.selectCount(Wrappers.<SystemDeptDO>lambdaQuery()
                .eq(SystemDeptDO::getParentId, parentId)
                .eq(SystemDeptDO::getDeptName, deptName)
                .ne(excludeDeptId != null, SystemDeptDO::getDeptId, excludeDeptId));
        if (count != null && count > 0) {
            throw new ServiceException(DEPT_NAME_EXISTS);
        }
    }
}

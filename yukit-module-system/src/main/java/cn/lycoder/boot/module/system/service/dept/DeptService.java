package cn.lycoder.boot.module.system.service.dept;

import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptListReqVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptRespVO;
import cn.lycoder.boot.module.system.controller.admin.dept.vo.DeptUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.dept.SystemDeptDO;

import java.util.List;

/**
 * 部门管理 Service 接口
 */
public interface DeptService {

    /**
     * 创建部门（自动计算 ancestors 祖级链）
     */
    Long createDept(DeptCreateReqVO reqVO);

    /**
     * 更新部门（父变更时级联刷新子孙 ancestors）
     */
    void updateDept(DeptUpdateReqVO reqVO);

    /**
     * 删除部门（存在子部门时拒绝）
     */
    void deleteDept(Long deptId);

    /**
     * 获取部门详情
     */
    SystemDeptDO getDept(Long deptId);

    /**
     * 获取部门树
     */
    List<DeptRespVO> getDeptTree(DeptListReqVO reqVO);

    /**
     * 查询某部门自身 + 全部子孙部门 ID，供「本部门及以下」数据范围使用
     */
    List<Long> getSelfAndChildDeptIds(Long deptId);
}

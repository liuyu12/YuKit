package cn.lycoder.boot.module.system.controller.admin.dept.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 部门列表查询 Request VO（返回树形，故不分页）
 */
@Data
public class DeptListReqVO {

    /**
     * 部门名称，模糊匹配
     */
    @Schema(description = "部门名称", example = "研发")
    private String deptName;

    /**
     * 部门状态（0正常 1停用）
     */
    @Schema(description = "部门状态（0正常 1停用）", example = "0")
    private String status;
}

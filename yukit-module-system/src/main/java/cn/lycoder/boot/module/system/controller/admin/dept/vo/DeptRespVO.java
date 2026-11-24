package cn.lycoder.boot.module.system.controller.admin.dept.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门 Response VO（树形，children 由服务层组装）
 */
@Data
public class DeptRespVO implements Serializable {

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "100")
    private Long deptId;

    /**
     * 父部门ID
     */
    @Schema(description = "父部门ID", example = "0")
    private Long parentId;

    /**
     * 祖级列表
     */
    @Schema(description = "祖级列表", example = "0,100")
    private String ancestors;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "研发部门")
    private String deptName;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序", example = "1")
    private Integer orderNum;

    /**
     * 负责人
     */
    @Schema(description = "负责人", example = "若依")
    private String leader;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "15888888888")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "ry@163.com")
    private String email;

    /**
     * 部门状态（0正常 1停用）
     */
    @Schema(description = "部门状态（0正常 1停用）", example = "0")
    private String status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 子部门
     */
    @Schema(description = "子部门")
    private List<DeptRespVO> children;
}

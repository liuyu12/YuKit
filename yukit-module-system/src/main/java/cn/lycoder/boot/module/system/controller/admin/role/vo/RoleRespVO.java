package cn.lycoder.boot.module.system.controller.admin.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色 Response VO
 */
@Data
public class RoleRespVO implements Serializable {

    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "2")
    private Long roleId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "运营角色")
    private String roleName;

    /**
     * 角色权限字符串
     */
    @Schema(description = "角色权限字符串", example = "operator")
    private String roleKey;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序", example = "1")
    private Integer roleSort;

    /**
     * 数据范围
     */
    @Schema(description = "数据范围")
    private String dataScope;

    /**
     * 角色状态（0正常 1停用）
     */
    @Schema(description = "角色状态（0正常 1停用）", example = "0")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "负责日常运营")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private LocalDateTime createTime;
}

package cn.lycoder.boot.module.system.controller.admin.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单 Response VO（树形，children 由服务层组装）
 */
@Data
public class MenuRespVO implements Serializable {

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID", example = "100")
    private Long menuId;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID", example = "0")
    private Long parentId;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序", example = "1")
    private Integer orderNum;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址", example = "user")
    private String path;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "system/user/index")
    private String component;

    /**
     * 路由参数
     */
    @Schema(description = "路由参数")
    private String query;

    /**
     * 路由名称
     */
    @Schema(description = "路由名称", example = "SystemUser")
    private String routeName;

    /**
     * 是否为外链（0是 1否）
     */
    @Schema(description = "是否为外链（0是 1否）", example = "1")
    private Integer isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    @Schema(description = "是否缓存（0缓存 1不缓存）", example = "0")
    private Integer isCache;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    @Schema(description = "菜单类型（M目录 C菜单 F按钮）", example = "C")
    private String menuType;

    /**
     * 显示状态（0显示 1隐藏）
     */
    @Schema(description = "显示状态（0显示 1隐藏）", example = "0")
    private String visible;

    /**
     * 菜单状态（0正常 1停用）
     */
    @Schema(description = "菜单状态（0正常 1停用）", example = "0")
    private String status;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识", example = "system:user:list")
    private String perms;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标", example = "user")
    private String icon;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "用户管理菜单")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 子菜单
     */
    @Schema(description = "子菜单")
    private List<MenuRespVO> children;
}

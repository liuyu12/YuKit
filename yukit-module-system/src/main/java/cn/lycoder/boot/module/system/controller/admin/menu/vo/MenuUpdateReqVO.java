package cn.lycoder.boot.module.system.controller.admin.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单更新 Request VO
 * <p>
 * menuId 由路径参数传入并覆盖
 */
@Data
public class MenuUpdateReqVO {

    /**
     * 菜单ID（由路径参数传入并覆盖，请求体可省略）
     */
    @Schema(description = "菜单ID（由路径参数传入并覆盖，请求体可省略）", example = "100")
    private Long menuId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    /**
     * 父菜单ID，根节点为 0
     */
    @NotNull(message = "父菜单不能为空")
    @Schema(description = "父菜单ID，根节点为 0", example = "0")
    private Long parentId;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
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
    @NotBlank(message = "菜单类型不能为空")
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
}

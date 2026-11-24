package cn.lycoder.boot.module.system.controller.admin.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单列表查询 Request VO（树形列表，不分页）
 */
@Data
public class MenuListReqVO {

    /**
     * 菜单名称，模糊匹配
     */
    @Schema(description = "菜单名称，模糊匹配", example = "用户")
    private String menuName;

    /**
     * 菜单状态（0正常 1停用）
     */
    @Schema(description = "菜单状态（0正常 1停用）", example = "0")
    private String status;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    @Schema(description = "菜单类型（M目录 C菜单 F按钮）", example = "C")
    private String menuType;
}

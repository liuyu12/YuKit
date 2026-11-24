package cn.lycoder.boot.module.system.controller.admin.menu.vo;

import lombok.Data;

/**
 * 菜单列表查询 Request VO（树形列表，不分页）
 */
@Data
public class MenuListReqVO {

    /**
     * 菜单名称，模糊匹配
     */
    private String menuName;

    /**
     * 菜单状态（0正常 1停用）
     */
    private String status;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    private String menuType;
}

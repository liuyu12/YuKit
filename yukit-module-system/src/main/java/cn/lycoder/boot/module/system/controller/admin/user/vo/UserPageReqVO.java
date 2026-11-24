package cn.lycoder.boot.module.system.controller.admin.user.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageReqVO extends PageParam {

    /**
     * 用户账号，模糊匹配
     */
    private String userName;

    /**
     * 手机号码，模糊匹配
     */
    private String phonenumber;

    /**
     * 账号状态（0正常 1停用），精确匹配
     */
    private String status;
}

package cn.lycoder.boot.module.system.enums;

import cn.lycoder.boot.framework.common.pojo.ErrorCode;

/**
 * system 模块业务错误码
 * <p>
 * 命名段：1-002-xxx-xxx，避免与全局错误码及各业务模块冲突
 */
public final class SystemErrorCodeConstants {

    private SystemErrorCodeConstants() {
        throw new UnsupportedOperationException("常量类，禁止实例化");
    }

    // ========== 认证模块 1-002-000-xxx ==========

    public static final ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1002000000, "登录失败，账号密码不正确");
    public static final ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1002000001, "登录失败，账号被禁用");
    public static final ErrorCode AUTH_LOGIN_CAPTCHA_CODE_ERROR = new ErrorCode(1002000002, "验证码不正确，原因：{}");
    public static final ErrorCode AUTH_LOGOUT_FAILED = new ErrorCode(1002000003, "退出登录失败");
    public static final ErrorCode AUTH_LOGIN_ACCOUNT_LOCKED = new ErrorCode(1002000004, "登录失败，账号已被锁定");
    public static final ErrorCode ONLINE_SESSION_NOT_EXISTS = new ErrorCode(1002000005, "会话不存在或已下线");

    // ========== 用户模块 1-002-001-xxx ==========

    public static final ErrorCode USER_NOT_EXISTS = new ErrorCode(1002001000, "用户不存在");
    public static final ErrorCode USER_NAME_EXISTS = new ErrorCode(1002001001, "用户名已被占用");

    // ========== 角色模块 1-002-002-xxx ==========

    public static final ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1002002000, "角色不存在");
    public static final ErrorCode ROLE_KEY_EXISTS = new ErrorCode(1002002001, "角色权限字符已存在");
    public static final ErrorCode ROLE_NAME_EXISTS = new ErrorCode(1002002002, "角色名称已存在");

    // ========== 菜单模块 1-002-003-xxx ==========

    public static final ErrorCode MENU_NOT_EXISTS = new ErrorCode(1002003000, "菜单不存在");
    public static final ErrorCode MENU_NAME_EXISTS = new ErrorCode(1002003001, "菜单名称已存在于同一父菜单下");
    public static final ErrorCode MENU_EXITS_CHILDREN = new ErrorCode(1002003002, "存在子菜单，不允许删除");
    public static final ErrorCode MENU_PARENT_NOT_EXISTS = new ErrorCode(1002003003, "父菜单不存在");
    public static final ErrorCode MENU_PARENT_ERROR = new ErrorCode(1002003004, "不能设置自己为父菜单");
    public static final ErrorCode MENU_PERMS_EXISTS = new ErrorCode(1002003005, "权限标识已存在");

    // ========== 字典模块 1-002-004-xxx ==========

    public static final ErrorCode DICT_TYPE_NOT_EXISTS = new ErrorCode(1002004000, "字典类型不存在");
    public static final ErrorCode DICT_TYPE_DUPLICATE = new ErrorCode(1002004001, "字典类型标识已存在");
    public static final ErrorCode DICT_TYPE_HAS_DATA = new ErrorCode(1002004002, "该字典类型下存在字典数据，不允许删除");
    public static final ErrorCode DICT_DATA_NOT_EXISTS = new ErrorCode(1002004003, "字典数据不存在");
    public static final ErrorCode DICT_DATA_DUPLICATE = new ErrorCode(1002004004, "同一字典类型下键值已存在");
    public static final ErrorCode DICT_DATA_TYPE_NOT_EXISTS = new ErrorCode(1002004005, "字典类型不存在，请先创建字典类型");

    // ========== 部门模块 1-002-005-xxx ==========

    public static final ErrorCode DEPT_NOT_EXISTS = new ErrorCode(1002005000, "部门不存在");
    public static final ErrorCode DEPT_NAME_EXISTS = new ErrorCode(1002005001, "部门名称已存在于同一父部门下");
    public static final ErrorCode DEPT_EXITS_CHILDREN = new ErrorCode(1002005002, "存在子部门，不允许删除");
    public static final ErrorCode DEPT_PARENT_NOT_EXISTS = new ErrorCode(1002005003, "父部门不存在");
    public static final ErrorCode DEPT_PARENT_ERROR = new ErrorCode(1002005004, "不能设置自己为父部门");
    public static final ErrorCode DEPT_PARENT_IS_CHILD = new ErrorCode(1002005005, "不能设置自己的子部门为父部门");

    // ========== 定时任务模块 1-002-006-xxx ==========

    public static final ErrorCode JOB_NOT_EXISTS = new ErrorCode(1002006000, "定时任务不存在");
    public static final ErrorCode JOB_NAME_GROUP_DUPLICATE = new ErrorCode(1002006001, "任务名称与任务组名组合已存在");
    public static final ErrorCode JOB_INCORRECT_CRON_EXPRESSION = new ErrorCode(1002006002, "cron 表达式不合法");
    public static final ErrorCode JOB_INVOKE_TARGET_INVALID = new ErrorCode(1002006003, "调用目标不合法或 Bean 方法未标注 @JobHandler");
    public static final ErrorCode JOB_RUNNING_FORBID_CONCURRENT = new ErrorCode(1002006004, "任务禁止并发且正在执行中");
}

package cn.lycoder.boot.framework.common.enums;

import cn.lycoder.boot.framework.common.pojo.ErrorCode;

/**
 * 全局错误码常量
 * <p>
 * 业务错误码请勿加在这里，放到各业务模块自己的 *ErrorCodeConstants 中
 */
public final class GlobalErrorCodeConstants {

    private GlobalErrorCodeConstants() {
        throw new UnsupportedOperationException("常量类，禁止实例化");
    }

    public static final ErrorCode SUCCESS = new ErrorCode(0, "成功");

    // ========== 客户端错误段 ==========

    public static final ErrorCode BAD_REQUEST = new ErrorCode(400, "请求参数不正确");
    public static final ErrorCode UNAUTHORIZED = new ErrorCode(401, "账号未登录");
    public static final ErrorCode FORBIDDEN = new ErrorCode(403, "没有该操作权限");
    public static final ErrorCode NOT_FOUND = new ErrorCode(404, "请求未找到");
    public static final ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(405, "请求方法不正确");
    public static final ErrorCode UNSUPPORTED_MEDIA_TYPE = new ErrorCode(415, "请求的 Content-Type 不正确");
    public static final ErrorCode LOCKED = new ErrorCode(423, "请求失败，请稍后重试"); // 并发请求限制
    public static final ErrorCode TOO_MANY_REQUESTS = new ErrorCode(429, "请求过于频繁，请稍后重试");

    // ========== 服务端错误段 ==========

    public static final ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, "系统异常");
    public static final ErrorCode NOT_IMPLEMENTED = new ErrorCode(501, "功能未实现/未开启");
    public static final ErrorCode ERROR_CONFIGURATION = new ErrorCode(502, "错误的配置项");

    // ========== 自定义错误段 ==========

    public static final ErrorCode REPEATED_REQUESTS = new ErrorCode(900, "重复请求，请稍后重试");
    public static final ErrorCode UNKNOWN = new ErrorCode(999, "未知错误");
}
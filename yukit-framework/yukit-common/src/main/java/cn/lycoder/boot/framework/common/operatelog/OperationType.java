package cn.lycoder.boot.framework.common.operatelog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 操作日志分类
 * <p>
 * 与常见后台「操作日志」的操作类型对齐，供 {@link OperationLog#type()} 标注业务动作语义，
 * 落库后用于检索归类。新增取值不影响既有数据。
 */
@Getter
@RequiredArgsConstructor
public enum OperationType {

    /** 其它：未明确归类的操作 */
    OTHER(0, "其它"),
    /** 查询 */
    QUERY(1, "查询"),
    /** 新增 */
    INSERT(2, "新增"),
    /** 修改 */
    UPDATE(3, "修改"),
    /** 删除 */
    DELETE(4, "删除"),
    /** 授权 */
    GRANT(5, "授权"),
    /** 导出 */
    EXPORT(6, "导出"),
    /** 导入 */
    IMPORT(7, "导入"),
    /** 强退 */
    FORCE(8, "强退"),
    /** 登录 */
    LOGIN(9, "登录"),
    /** 登出 */
    LOGOUT(10, "登出");

    /**
     * 类型码，落库存该值，稳定且紧凑
     */
    private final int code;

    /**
     * 中文描述
     */
    private final String description;
}

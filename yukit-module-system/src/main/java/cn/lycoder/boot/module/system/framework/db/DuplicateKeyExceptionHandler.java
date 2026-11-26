package cn.lycoder.boot.module.system.framework.db;

import cn.lycoder.boot.framework.common.pojo.ErrorCode;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 唯一索引冲突处理器
 * <p>
 * Service 层的 selectCount 预校验挡不住"查—插"竞态（两个请求同时通过校验），最终由数据库唯一索引拦下。
 * 若不接住 {@link DuplicateKeyException}，它会一路落到 web starter 的 GlobalExceptionHandler
 * （其 {@code @ExceptionHandler(Exception.class)} 兜底），前端拿到 500 系统异常——竞态路径的
 * 表现就此与预校验路径不一致。
 * <p>
 * MySQL 的报错文本自带冲突索引名（{@code Duplicate entry 'x' for key 'sys_user.uk_sys_user_active_name'}），
 * 因此按索引名映射回对应的业务错误码，让两条路径返回完全相同的 code 与提示，前端无需区分。
 * 索引名同时写在 V11__unique_constraints.sql 的注释里，标注了对应的 Service 校验方法。
 * <p>
 * Order 数值须小于 GlobalExceptionHandler 的 100：多个 Advice 按优先级逐个匹配，
 * 排在后面就再也轮不到本类（Exception 兜底会先吞掉）。
 */
@Slf4j
@Order(-100)
@RestControllerAdvice
public class DuplicateKeyExceptionHandler {

    /**
     * 从 MySQL 报错文本中提取索引名；MySQL 8 带表名前缀（sys_user.uk_xxx），5.7 不带，统一取末段
     */
    private static final Pattern INDEX_NAME_PATTERN = Pattern.compile("for key '([^']+)'");

    /**
     * 冲突索引名 → 业务错误码。新增唯一索引时在此登记，未登记的落到兜底码
     */
    private static final Map<String, ErrorCode> INDEX_ERROR_CODES = Map.of(
            // V11：逻辑删除表用生成列，仅约束未删行
            "uk_sys_user_active_name", SystemErrorCodeConstants.USER_NAME_EXISTS,
            "uk_sys_role_active_key", SystemErrorCodeConstants.ROLE_KEY_EXISTS,
            "uk_sys_dept_active_parent_name", SystemErrorCodeConstants.DEPT_NAME_EXISTS,
            // V11：物理删除表，直接全局唯一
            "uk_sys_post_name", SystemErrorCodeConstants.POST_NAME_DUPLICATE,
            "uk_sys_dict_data_type_value", SystemErrorCodeConstants.DICT_DATA_DUPLICATE,
            "uk_sys_menu_parent_name", SystemErrorCodeConstants.MENU_NAME_EXISTS,
            // V1/V6/V9 既有唯一索引，此前无兜底，一并纳入
            "uk_sys_config_key", SystemErrorCodeConstants.CONFIG_KEY_EXISTS,
            "dict_type", SystemErrorCodeConstants.DICT_TYPE_DUPLICATE,
            "uk_sys_job_name_group", SystemErrorCodeConstants.JOB_NAME_GROUP_DUPLICATE,
            "uk_sys_post_code", SystemErrorCodeConstants.POST_CODE_DUPLICATE);

    /**
     * 唯一键冲突：译为对应业务错误码，HTTP 200 + 业务 code，与 Service 预校验保持一致
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e) {
        String index = resolveIndexName(e);
        ErrorCode errorCode = INDEX_ERROR_CODES.getOrDefault(index, SystemErrorCodeConstants.DATA_UNIQUE_CONFLICT);
        // 只记索引名与错误码，不透出冲突值：报错文本里带着用户提交的明文，不该进对外响应
        log.warn("[handleDuplicateKey] 唯一索引冲突，index={}，code={}", index, errorCode.getCode(), e);
        return Result.error(errorCode);
    }

    /**
     * 提取冲突索引名，解析不出时返回空串（走兜底码）
     */
    private String resolveIndexName(DuplicateKeyException e) {
        Throwable cause = e.getMostSpecificCause();
        String message = cause != null ? cause.getMessage() : e.getMessage();
        if (message == null) {
            return "";
        }
        Matcher matcher = INDEX_NAME_PATTERN.matcher(message);
        if (!matcher.find()) {
            return "";
        }
        String raw = matcher.group(1);
        int dot = raw.lastIndexOf('.');
        return dot >= 0 ? raw.substring(dot + 1).replace("`", "") : raw.replace("`", "");
    }
}

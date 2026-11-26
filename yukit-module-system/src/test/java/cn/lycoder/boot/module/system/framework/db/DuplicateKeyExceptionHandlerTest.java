package cn.lycoder.boot.module.system.framework.db;

import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DuplicateKeyExceptionHandler} 索引名 → 业务错误码映射单元测试
 */
class DuplicateKeyExceptionHandlerTest {

    private final DuplicateKeyExceptionHandler handler = new DuplicateKeyExceptionHandler();

    /**
     * 还原生产形态：外层是 Spring 翻译后的异常（消息里没有 for key），
     * 索引名只出现在最内层 SQLException 的 message 里，用于验证取的是 mostSpecificCause
     */
    private DuplicateKeyException duplicate(String innerMessage) {
        SQLException cause = new SQLIntegrityConstraintViolationException(innerMessage, "23000", 1062);
        return new DuplicateKeyException("Statement executed; nested exception is", cause);
    }

    @Test
    @DisplayName("V11 新增的逻辑删除表生成列索引：译为各自的业务码")
    void mapsGeneratedColumnIndexes() {
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry 'admin' for key 'sys_user.uk_sys_user_active_name'")).getCode())
                .isEqualTo(SystemErrorCodeConstants.USER_NAME_EXISTS.getCode());
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry 'common' for key 'sys_role.uk_sys_role_active_key'")).getCode())
                .isEqualTo(SystemErrorCodeConstants.ROLE_KEY_EXISTS.getCode());
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry '101-研发' for key 'sys_dept.uk_sys_dept_active_parent_name'")).getCode())
                .isEqualTo(SystemErrorCodeConstants.DEPT_NAME_EXISTS.getCode());
    }

    @Test
    @DisplayName("V11 新增的物理删除表索引 + 既有索引同样覆盖")
    void mapsPlainIndexes() {
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry 'se' for key 'sys_post.uk_sys_post_name'")).getCode())
                .isEqualTo(SystemErrorCodeConstants.POST_NAME_DUPLICATE.getCode());
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry 'sys_normal_disable-0' for key 'sys_dict_data.uk_sys_dict_data_type_value'"))
                .getCode())
                .isEqualTo(SystemErrorCodeConstants.DICT_DATA_DUPLICATE.getCode());
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry '2-用户管理' for key 'sys_menu.uk_sys_menu_parent_name'")).getCode())
                .isEqualTo(SystemErrorCodeConstants.MENU_NAME_EXISTS.getCode());
        // 既有索引：此前无任何兜底，撞上就是 500
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry 'sys.aaa' for key 'sys_config.uk_sys_config_key'")).getCode())
                .isEqualTo(SystemErrorCodeConstants.CONFIG_KEY_EXISTS.getCode());
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry 'x' for key 'sys_dict_type.dict_type'")).getCode())
                .isEqualTo(SystemErrorCodeConstants.DICT_TYPE_DUPLICATE.getCode());
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry 'job-DEFAULT' for key 'sys_job.uk_sys_job_name_group'")).getCode())
                .isEqualTo(SystemErrorCodeConstants.JOB_NAME_GROUP_DUPLICATE.getCode());
    }

    @Test
    @DisplayName("未登记的索引名落兜底码，不抛异常也不返回 500")
    void fallsBackForUnknownIndex() {
        Result<Void> result = handler.handleDuplicateKey(
                duplicate("Duplicate entry '9' for key 'sys_xxx.uk_not_registered'"));
        assertThat(result.getCode()).isEqualTo(SystemErrorCodeConstants.DATA_UNIQUE_CONFLICT.getCode());
    }

    @Test
    @DisplayName("消息解析不出索引名（非 MySQL 报文 / 无 message）仍归为唯一冲突")
    void toleratesUnparsableMessage() {
        assertThat(handler.handleDuplicateKey(duplicate("Duplicate entry found")).getCode())
                .isEqualTo(SystemErrorCodeConstants.DATA_UNIQUE_CONFLICT.getCode());
        assertThat(handler.handleDuplicateKey(new DuplicateKeyException((String) null)).getCode())
                .isEqualTo(SystemErrorCodeConstants.DATA_UNIQUE_CONFLICT.getCode());
    }

    @Test
    @DisplayName("对外响应不回显冲突明文：报错文本含用户提交的账号名，不得透出")
    void doesNotLeakConflictingValue() {
        Result<Void> result = handler.handleDuplicateKey(
                duplicate("Duplicate entry 'zhangsan' for key 'sys_user.uk_sys_user_active_name'"));
        assertThat(result.getMsg()).doesNotContain("zhangsan")
                .isEqualTo(SystemErrorCodeConstants.USER_NAME_EXISTS.getMsg());
    }

    @Test
    @DisplayName("MySQL 5.7 报文不带表名前缀，同样能取到索引名")
    void mapsIndexWithoutTablePrefix() {
        assertThat(handler.handleDuplicateKey(
                duplicate("Duplicate entry 'admin' for key 'uk_sys_user_active_name'")).getCode())
                .isEqualTo(SystemErrorCodeConstants.USER_NAME_EXISTS.getCode());
    }
}

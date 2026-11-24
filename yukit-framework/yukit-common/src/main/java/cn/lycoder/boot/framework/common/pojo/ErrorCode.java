package cn.lycoder.boot.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 错误码
 *
 * 分段约定：
 * 0        成功
 * 4xx/5xx  全局错误（复用 HTTP 状态码语义）
 * 1_XXX_XXX 业务模块错误码（如 1_002_001 表示 system 模块），从 10 亿以内自定义扩展
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorCode implements Serializable {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误提示
     */
    private String msg;
}
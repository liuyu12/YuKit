package cn.lycoder.boot.framework.common.pojo;

import cn.lycoder.boot.framework.common.enums.GlobalErrorCodeConstants;
import cn.lycoder.boot.framework.common.exception.ServiceException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 统一响应结果
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    /**
     * 错误码，0 表示成功
     *
     * @see GlobalErrorCodeConstants
     */
    private Integer code;

    /**
     * 错误提示，失败时展示给用户
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(GlobalErrorCodeConstants.SUCCESS.getCode());
        result.setMsg("");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMsg());
    }

    public static <T> Result<T> error(ServiceException ex) {
        return error(ex.getCode(), ex.getMessage());
    }

    @JsonIgnore
    public boolean isSuccess() {
        return Objects.equals(GlobalErrorCodeConstants.SUCCESS.getCode(), code);
    }

    @JsonIgnore
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 校验结果，失败则抛出异常（用于 Feign/RPC 式调用链的错误传播，暂备用）
     */
    public void checkError() throws ServiceException {
        if (isError()) {
            throw new ServiceException(code, msg);
        }
    }
}
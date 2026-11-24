package cn.lycoder.boot.framework.common.exception;

import cn.lycoder.boot.framework.common.enums.GlobalErrorCodeConstants;
import cn.lycoder.boot.framework.common.pojo.ErrorCode;
import lombok.Getter;

/**
 * 业务异常，携带错误码，由全局异常处理器统一转换为 Result
 */
@Getter
public class ServiceException extends RuntimeException {

    /**
     * 错误码，非 final 以便异常处理器兜底改写
     */
    private final Integer code;

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message) {
        super(message);
        this.code = GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode();
    }

    /**
     * 链式定制错误码：exception(ServiceExceptionUtil.invalidParamException("xx"))
     */
    public boolean isCode(Integer code) {
        return this.code.equals(code);
    }
}
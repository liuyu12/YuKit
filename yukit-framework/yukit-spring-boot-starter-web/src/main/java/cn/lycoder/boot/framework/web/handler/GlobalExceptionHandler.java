package cn.lycoder.boot.framework.web.handler;

import cn.lycoder.boot.framework.common.enums.GlobalErrorCodeConstants;
import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器：将异常统一转换为 {@link Result} 结构
 * <p>
 * 声明顺序无关，Spring 按异常类型的继承层级择优匹配；越具体越优先。
 * <p>
 * Order 垫底：Exception 兜底不得抢在其他 Advice（如 security 的 SaTokenExceptionHandler）前面，
 * 否则未登录/无权限等异常会被误归为 500 系统异常
 */
@Slf4j
@Order(100)
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment environment;

    // ========== 业务异常 ==========

    /**
     * 业务异常：原样透出错误码与提示
     */
    @ExceptionHandler(ServiceException.class)
    public Result<Void> handleServiceException(ServiceException e) {
        log.warn("[handleServiceException] 业务异常，code={}", e.getCode(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    // ========== 参数校验 ==========

    /**
     * 请求体 @Valid 校验失败（@RequestBody 场景）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        return buildBindExceptionResult(e.getBindingResult());
    }

    /**
     * 表单/查询参数绑定校验失败（@ModelAttribute 场景，如分页 GET）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        return buildBindExceptionResult(e.getBindingResult());
    }

    /**
     * 方法参数直接标注的校验失败（类上 @Validated + @RequestParam/@PathVariable 单参数场景），
     * 如 @GetMapping 方法里的 @Min(1) Integer id，不接住会误落入兜底的 500 分支
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> {
                    // 取字段名拼消息：path 形如 getUserById.id，末段是参数名
                    String[] parts = v.getPropertyPath().toString().split("\\.");
                    String field = parts.length > 0 ? parts[parts.length - 1] : "";
                    return field.isEmpty() ? v.getMessage() : field + " " + v.getMessage();
                })
                .orElse(GlobalErrorCodeConstants.BAD_REQUEST.getMsg());
        log.warn("[handleConstraintViolation] 参数校验失败：{}", msg);
        return Result.error(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 缺少必填的请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("[handleMissingParam] 缺少必填参数：{}", e.getParameterName());
        return Result.error(GlobalErrorCodeConstants.BAD_REQUEST.getCode(),
                "缺少必填参数：" + e.getParameterName());
    }

    /**
     * 参数类型不匹配，如 ?userId=abc 无法转成 Long
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("[handleTypeMismatch] 参数类型不正确：{}", e.getName());
        return Result.error(GlobalErrorCodeConstants.BAD_REQUEST.getCode(),
                "参数类型不正确：" + e.getName());
    }

    /**
     * 请求体无法解析，如 JSON 格式错误
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("[handleNotReadable] 请求体格式错误", e);
        return Result.error(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "请求体格式错误，请检查 JSON 是否合法");
    }

    // ========== 路由 / 方法 ==========

    /**
     * 请求方法不支持，如用 GET 调 POST 接口
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("[handleMethodNotSupported] 请求方法不正确：{}", e.getMethod());
        return Result.error(GlobalErrorCodeConstants.METHOD_NOT_ALLOWED.getCode(),
                String.format("请求方法不正确，支持的方法：%s", String.join("/", e.getSupportedMethods() == null
                        ? new String[]{} : e.getSupportedMethods())));
    }

    /**
     * Content-Type 不支持，如需要 application/json 却传了 text/plain
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("[handleMediaTypeNotSupported] Content-Type 不支持", e);
        return Result.error(GlobalErrorCodeConstants.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * 资源/接口不存在（Spring 6.1+ 静态资源与未匹配路由）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("[handleNoResourceFound] 请求未找到：{}", e.getResourcePath());
        return Result.error(GlobalErrorCodeConstants.NOT_FOUND);
    }

    // ========== 兜底 ==========

    /**
     * 兜底异常：生产环境只回通用提示，避免堆栈细节泄漏给前端；
     * dev 环境透出异常类名与消息，方便接口调试时不看服务端日志
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("[handleException] 系统异常", e);
        if (isDevProfile()) {
            String detail = e.getClass().getSimpleName() + ": " + e.getMessage();
            return Result.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "系统异常（dev）：" + truncate(detail, 300));
        }
        return Result.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
    }

    private boolean isDevProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equals(profile) || "local".equals(profile)) {
                return true;
            }
        }
        return false;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    /**
     * 提取绑定校验结果的首条字段错误消息
     */
    private Result<Void> buildBindExceptionResult(BindingResult bindingResult) {
        String msg = bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(GlobalErrorCodeConstants.BAD_REQUEST.getMsg());
        log.warn("[buildBindExceptionResult] 参数校验失败：{}", msg);
        return Result.error(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), msg);
    }
}

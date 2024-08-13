package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 全局异常处理器
 *
 * <p>规定以 JSON 数据格式返回给前端
 *
 * @author liujue
 * @description
 */
@Slf4j
@RestControllerAdvice   // @ResponseBody + @ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     */
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 该异常枚举错误码为 500
    public RestErrorResponse customException(XueChengPlusException exception) {
        log.error("出现业务异常：{}", exception.getErrMessage());
        return new RestErrorResponse(exception.getErrMessage());
    }

    /**
     * 捕获通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception exception) {
        log.error("出现系统异常：{}", exception.getMessage());
        // org.springframework.security.access.AccessDeniedException: 不允许访问
        if ("不允许访问".equals(exception.getMessage())) {
            return new RestErrorResponse("您没有权限操作此功能");
        }
        return new RestErrorResponse(exception.getMessage());
    }

    /**
     * 捕获 DTO 字段校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder stringBuilder = new StringBuilder();
        fieldErrors.forEach(fieldError -> stringBuilder.append(fieldError.getDefaultMessage()).append(","));
        log.error(stringBuilder.toString());
        return new RestErrorResponse(stringBuilder.toString());
    }
}
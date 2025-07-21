package com.katomegumi.zxpicturebackend.core.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器 捕获BusinessException 和 RuntimeException
 *
 * @author lirui
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 预定义 NotLoginException 类型对应的错误信息
    private static final Map<String, String> NOT_LOGIN_MESSAGES = new HashMap<>();

    static {
        NOT_LOGIN_MESSAGES.put(NotLoginException.NOT_TOKEN, "当前会话未登录,请先登录");
        NOT_LOGIN_MESSAGES.put(NotLoginException.INVALID_TOKEN, "当前会话未登录，请先登录");
        NOT_LOGIN_MESSAGES.put(NotLoginException.TOKEN_TIMEOUT, "登录已过期，请重新登录");
        NOT_LOGIN_MESSAGES.put(NotLoginException.BE_REPLACED, "您的账号已在其他地方登录，当前会话已下线");
        NOT_LOGIN_MESSAGES.put(NotLoginException.KICK_OUT, "您已被管理员强制下线");
        NOT_LOGIN_MESSAGES.put(NotLoginException.TOKEN_FREEZE, "您的账号已被冻结，请联系管理员");
        NOT_LOGIN_MESSAGES.put(NotLoginException.NO_PREFIX, "Token 格式不正确，缺少指定前缀");
    }


    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> baseResponseException(BusinessException e) {
        log.error("BusinessException 业务异常:{}", e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }


    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException 运行异常:{}", e.getMessage(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }


    //全局异常拦截：捕获 Sa-Token 的 NotLoginException 异常
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> handleNotLoginException(NotLoginException e) {
        // 打印堆栈信息，方便调试和排查问题
        log.error("Sa-Token 登录异常: {}", e.getMessage(), e);
        String message = NOT_LOGIN_MESSAGES.getOrDefault(e.getType(), "当前会话未登录，请先登录");
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, message);
    }

    //捕获 sa-token 异常
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e) {
        log.error("NotPermissionException 异常:{}", e.getMessage(), e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, e.getMessage());
    }

}

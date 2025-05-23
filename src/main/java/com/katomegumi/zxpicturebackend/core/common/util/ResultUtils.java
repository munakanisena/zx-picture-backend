package com.katomegumi.zxpicturebackend.core.common.util;

import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;

/**
 * 简化响应
 * @author Megumi
 */
public class ResultUtils {

    public static BaseResponse<Boolean> success() {
        return new BaseResponse<>(0,true,"ok");
    }
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"ok");
    }

    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code,null,message);
    }

    public static BaseResponse<?> error(ErrorCode errorCode, String message) {

        return new BaseResponse<>(errorCode.getCode(),null,message);
    }
}

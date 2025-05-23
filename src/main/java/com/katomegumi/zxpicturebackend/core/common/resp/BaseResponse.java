package com.katomegumi.zxpicturebackend.core.common.resp;


import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 响应类
 * @param <T>
 */

@Data
public class BaseResponse <T> implements Serializable {
    private int code;

    private T data;

    private String message;

    public BaseResponse(int code,T data,String message) {
        this.code = code;
        this.data=data;
        this.message = message;
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage());
    }
}

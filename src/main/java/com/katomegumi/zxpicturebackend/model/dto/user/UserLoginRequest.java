package com.katomegumi.zxpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lirui
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -3429351205037970541L;
    /**
     * 用户账号 或者 邮箱地址
     */
    private String emailOrUsername;

    /**
     * 用户密码
     */
    private String password;
}

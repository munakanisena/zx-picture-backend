package com.katomegumi.zxpicturebackend.model.dto.user;


import lombok.Data;

import java.io.Serializable;

/**
 * 注册用户 dto
 * @author katoMegumi
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1195110832618584472L;

    /**
     * 用户名
     */
    private String name;

    /**
     * 用户邮箱
     */
    private String email;

    /*
     * 邮箱验证码
     */
    private String captcha;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;

}

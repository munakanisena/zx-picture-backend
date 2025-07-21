package com.katomegumi.zxpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : Megumi
 * @description :密码重置请求
 * @createDate : 2025/5/9 下午4:20
 */
@Data
public class UserResetPasswordRequest implements Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 邮件验证码
     */
    private String captcha;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String confirmPassword;
}


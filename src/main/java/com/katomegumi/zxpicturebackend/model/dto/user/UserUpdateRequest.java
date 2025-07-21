package com.katomegumi.zxpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员 用户更新
 *
 * @author Megumi
 */
@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 用户id
     */
    private Long id;

    /**
     * 登录名及昵称
     */
    private String name;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户简介
     */
    private String introduction;

    /**
     * 会员编号
     */
    private Long vipNumber;

    /**
     * 用户角色（USER-普通用户, ADMIN-管理员）
     */
    private String role;

    /**
     * 是否为会员;0-否 1-是
     */
    private Integer isVip;

    /**
     * 是否禁用（0-正常, 1-禁用）
     */
    private Integer isDisabled;

}

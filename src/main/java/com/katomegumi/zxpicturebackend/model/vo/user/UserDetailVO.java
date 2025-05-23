package com.katomegumi.zxpicturebackend.model.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Megumi
 * @description : 给其他用户返回的 用户信息
 */
@Data
public class UserDetailVO implements Serializable{

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 登录名及昵称
     */
    private String username;


    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户性别;0-男 1-女
     */
    private Integer userSex;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 出生日期
     */
    private Date birthday;

    /**
     * 会员编号
     */
    private Long vipNumber;

    /**
     * 用户角色（USER-普通用户, ADMIN-管理员）
     */
    private String userRole;

    /**
     * 是否为会员;0-否 1-是
     */
    private Integer isVip;


    private static final long serialVersionUID = 1L;

}

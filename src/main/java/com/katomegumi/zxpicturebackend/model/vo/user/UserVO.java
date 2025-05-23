package com.katomegumi.zxpicturebackend.model.vo.user;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : Megumi
 * @description : 给管理员返回的 用户信息
 * @createDate : 2025/5/9 下午9:47
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 登录名及昵称
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 用户手机号
     */
    private String userPhone;

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

    /**
     * 是否禁用（0-正常, 1-禁用）
     */
    private Integer isDisabled;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


}


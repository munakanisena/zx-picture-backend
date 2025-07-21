package com.katomegumi.zxpicturebackend.model.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Megumi
 * @description :用户信息
 */
@Data
public class UserDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 登录名及昵称
     */
    private String name;

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
     * 创建时间
     */
    private Date createTime;
}

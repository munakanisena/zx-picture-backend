package com.katomegumi.zxpicturebackend.model.dto.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : Megumi
 * @description : 用户编辑请求
 * @createDate : 2025/5/10 上午11:56
 */
@Data
public class UserEditRequest implements Serializable {

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
     * 手机号 //todo 预留
     */
    private String userPhone;

    /**
     * 用户性别;0-男 1-女;2-保密(默认)
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


}


package com.katomegumi.zxpicturebackend.manager.email.model;

import lombok.Data;


import java.io.Serializable;

/**
 * @author : Megumi
 * @description : 邮件地址
 * @createDate : 2025/5/7 下午1:44
 */
@Data
public class EmailRequest implements Serializable {
    private static final long serialVersionUID = 1L;


    private String userEmail;

}


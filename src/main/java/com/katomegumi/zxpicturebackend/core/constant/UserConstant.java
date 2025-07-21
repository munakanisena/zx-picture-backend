package com.katomegumi.zxpicturebackend.core.constant;

import lombok.experimental.UtilityClass;

/**
 * user常量类
 *
 * @author katoMegumi
 */
@UtilityClass
public class UserConstant {
    //  region 权限
    /**
     * 默认角色
     */
    public static final String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    public static final String ADMIN_ROLE = "admin";

    // endregion

    //加盐值
    public static final String SALT = "Megumi";
}

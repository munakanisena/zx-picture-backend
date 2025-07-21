package com.katomegumi.zxpicturebackend.core.constant;

import lombok.experimental.UtilityClass;

/**
 * @author : katoMegumi
 * @description : controller 常量
 * @createDate : 2025/5/3 下午7:05
 */
@UtilityClass
public class ApiRouterConstant {

    /**
     * 主页请求路径前缀
     */
    public static final String API_HOME_URL_PREFIX = "/home";

    /**
     * 用户请求路径前缀
     */
    public static final String API_USER_URL_PREFIX = "/user";

    /**
     * 空间请求路径前缀
     */
    public static final String API_SPACE_URL_PREFIX = "/space";

    /**
     * 图片请求路径前缀
     */
    public static final String API_PICTURE_URL_PREFIX = "/picture";

    /**
     * 图片分类请求路径前缀
     */
    public static final String API_PICTURE_CATEGORY_URL_PREFIX = API_PICTURE_URL_PREFIX + "/category";

    /**
     * 空间用户请求路径前缀
     */
    public static final String API_SPACE_USER_URL_PREFIX = API_SPACE_URL_PREFIX + "/user";

    /**
     * 空间分析请求路径前缀
     */
    public static final String API_SPACE_ANALYZE_URL_PREFIX = "/analyze" + API_SPACE_URL_PREFIX;


}


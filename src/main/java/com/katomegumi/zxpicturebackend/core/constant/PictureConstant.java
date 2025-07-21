package com.katomegumi.zxpicturebackend.core.constant;

import lombok.experimental.UtilityClass;

/**
 * @author : Megumi
 * @description : 图片常量
 * @createDate : 2025/5/25 下午4:52
 */
@UtilityClass
public class PictureConstant {

    /**
     * cos存储路径图片前缀
     */
    public static final String PICTURE_PREFIX = "images/";

    /**
     * 公共图片存储路径前缀
     */
    public static final String PUBLIC_PICTURE_PREFIX = PICTURE_PREFIX + "public/";

    /**
     * 空间图片存储路径前缀
     */
    public static final String SPACE_PICTURE_PREFIX = PICTURE_PREFIX + "space/";

    /**
     * AI扩图图片存储路径前缀
     */
    public static final String CAPTURE_PICTURE_PREFIX = PICTURE_PREFIX + "capture/";

}


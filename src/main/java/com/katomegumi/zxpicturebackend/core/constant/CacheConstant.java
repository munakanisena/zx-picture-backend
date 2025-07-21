package com.katomegumi.zxpicturebackend.core.constant;

import lombok.experimental.UtilityClass;

/**
 * @author : katoMegumi
 * @description : Cache常量  key:业务名称+数据名
 * @createDate : 2025/5/3 下午10:16
 */
@UtilityClass
public class CacheConstant {

    /**
     * 本项目 redis前缀
     */
    public static final String REDIS_CACHE_PREFIX = "zx-picture:";

    /**
     * Caffeine 缓存管理器
     */
    public static final String CAFFEINE_CACHE_MANAGER = "caffeineCacheManager";

    /**
     * Redis 缓存管理器
     */
    public static final String REDIS_CACHE_MANAGER = "redisCacheManager";

    /**
     * 首页图片缓存
     */
    public static final String HOME_PICTURE_CACHE_NAME = "homePictureCache";

    /**
     * 首页图片搜索缓存
     */
    public static final String PICTURE_SEARCH_CACHE_NAME = "homeSearchPictureCache";

    /**
     * 首页分类缓存
     */
    public static final String HOME_CATEGORY_CACHE_NAME = "homeCategoryCache";


    /**
     * 邮件缓存
     */
    public static class EMAIL {
        /**
         * 邮箱注册
         */
        public static final String REGISTER = REDIS_CACHE_PREFIX + "email:register:";

        /**
         * 重置密码
         */
        public static final String FORGOT = REDIS_CACHE_PREFIX + "email:forgot:";

        /**
         * 验证码
         */
        public static final String CAPTCHA = REDIS_CACHE_PREFIX + "captcha";

        /**
         * 发送次数
         */
        public static final String COUNT = REDIS_CACHE_PREFIX + "sendCount";

        /**
         * 最后发送时间
         */
        public static final String LAST_SEND_TIME = REDIS_CACHE_PREFIX + "lastSendTime";

        /**
         * html模板
         */
        public static final String GET_CAPTCHA = "captcha";

    }

    /**
     * 用户缓存
     */
    public static class USER {
        /**
         * 用户登录态键
         */
        public static final String USER_LOGIN_STATE = REDIS_CACHE_PREFIX + "loginState:userInfo:";
    }


    /**
     * 图片缓存
     */
    public static class PICTURE {
        /**
         * 图片互动(点赞总数) 前缀
         */
        public static final String PICTURE_INTERACTION_LIKE_KEY_PREFIX = REDIS_CACHE_PREFIX + "picture:interactions:like";

        /**
         * 图片互动(收藏总数) 前缀
         */
        public static final String PICTURE_INTERACTION_COLLECTION_KEY_PREFIX = REDIS_CACHE_PREFIX + "picture:interactions:collection";

        /**
         * 图片扩图任务(限制用户使用) 前缀
         */
        public static final String PICTURE_EXTEND_PREFIX = REDIS_CACHE_MANAGER + "picture:extend";

        /**
         * 图片限制 前缀
         */
        public static final String PICTURE_DOWNLOAD_PREFIX = REDIS_CACHE_PREFIX + "picture:download:";
    }
}


package com.katomegumi.zxpicturebackend.core.constant;

/**
 * @author : katoMegumi
 * @description : Redis常量
 * @createDate : 2025/5/3 下午10:16
 */
public class RedisConstant {




    public static class EMAIL{
        public static final String REGISTER = "email:register:";

        public static final String CAPTCHA="captcha";

        public static final String COUNT="sendCount";

        public static final String LAST_SEND_TIME ="lastSendTime";

        public static final String FORGOT="email:forgot:";
    }
}


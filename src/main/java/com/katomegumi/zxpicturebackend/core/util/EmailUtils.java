package com.katomegumi.zxpicturebackend.core.util;

import lombok.experimental.UtilityClass;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author : Megumi
 * @description :邮件工具类
 * @createDate : 2025/5/7 下午12:00
 */
@UtilityClass
public class EmailUtils {

    private static final String CONTENT ="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * 获取随机验证码
     * @param num 验证码长度
     * @return 验证码
     */
    public static String getRandomCaptcha(int num){
        //1.生成随机验证码
        int length = CONTENT.length();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            String rand = String.valueOf(CONTENT.charAt(new Random().nextInt(length)));
            stringBuilder.append(rand);
        }
        return stringBuilder.toString();
    }

}


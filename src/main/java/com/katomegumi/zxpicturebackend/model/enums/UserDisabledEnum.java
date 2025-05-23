package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author : Megumi
 * @description : 用户禁用枚举类
 * @createDate : 2025/5/10 上午11:21
 */
@Getter
public enum UserDisabledEnum {
    NORMAL( "正常",0),
    DISABLED( "禁用",1);

    private final String text;
    private final Integer value;


     UserDisabledEnum(String text,Integer value) {
         this.text = text;
         this.value = value;
    }


    /**
     * 是否禁用
     * @param value
     * @return
     */
    public static Boolean isDisabled(Integer value){
         return DISABLED.getValue().equals(value);
    }


    /**
     * 通过值 获取对应枚举类
     * @param value
     * @return
     */
    public static UserDisabledEnum fromValue(Integer value) {
         if (ObjUtil.isEmpty(value)){
             return null;
         }
        for (UserDisabledEnum userDisabledEnum : UserDisabledEnum.values()) {
            if (userDisabledEnum.value.equals(value)) {
                return userDisabledEnum;
            }
        }
        return null;
}
}


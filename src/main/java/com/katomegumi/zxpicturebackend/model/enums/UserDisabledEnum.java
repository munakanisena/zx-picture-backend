package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author : Megumi
 * @description : 用户禁用枚举类
 * @createDate : 2025/5/10 上午11:21
 */
@Getter
public enum UserDisabledEnum {

    NORMAL(0, "正常"),
    DISABLED(1, "禁用");

    private final int key;
    private final String label;

    UserDisabledEnum(int key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * 是否禁用
     */
    public static Boolean isDisabled(Integer key) {
        return DISABLED.getKey() == key;
    }

    /**
     * 根据 KEY 获取枚举
     * @param key 状态键值
     * @return 枚举对象，未找到时返回 null
     */
    public static UserDisabledEnum getEnumByKey(Integer key) {
        if (ObjUtil.isEmpty(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey() == key, values());
    }
}


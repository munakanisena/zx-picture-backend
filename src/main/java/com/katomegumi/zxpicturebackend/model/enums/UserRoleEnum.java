package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * Role枚举类  什么时候设置枚举类？  当变量是固定的几个值时 设置枚举类
 */
@Getter
public enum UserRoleEnum {

    USER("user", "用户"),
    ADMIN("admin", "管理员");

    private final String key;
    private final String label;

    UserRoleEnum(String key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * 根据 KEY 获取枚举
     * @param key 状态键值
     * @return 枚举对象，未找到时返回 null
     */
    public static UserRoleEnum getEnumByKey(String key) {
        if (ObjUtil.isEmpty(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey().equals(key), values());
    }

    /**
     * 判断是否是管理员
     */
    public static Boolean isAdmin(String key) {
        return ADMIN.key.equals(key);
    }
}
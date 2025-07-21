package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SpaceRoleEnum {

    VIEWER("viewer", "浏览者"),
    EDITOR("editor", "编辑者"),
    ADMIN("admin", "管理员");

    private final String key;
    private final String label;

    SpaceRoleEnum(String key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * 根据 KEY 获取枚举
     *
     * @param key 状态键值
     * @return 枚举对象，未找到时返回 null
     */
    public static SpaceRoleEnum getEnumByKey(String key) {
        if (ObjUtil.isEmpty(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey().equals(key), values());
    }

    /**
     * 获取所有枚举的 label 列表
     */
    public static List<String> getLabels() {
        return Arrays.stream(values())
                .map(SpaceRoleEnum::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有枚举的 key 列表
     */
    public static List<String> getKeys() {
        return Arrays.stream(values())
                .map(SpaceRoleEnum::getKey)
                .collect(Collectors.toList());
    }
}

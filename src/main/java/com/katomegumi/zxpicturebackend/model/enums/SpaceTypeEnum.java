package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author Megumi
 * 空间类型 枚举类
 */
@Getter
public enum SpaceTypeEnum {

    PUBLIC(0, "公共空间"),
    PRIVATE(1, "私有空间"),
    TEAM(2, "团队空间");

    private final Integer key;
    private final String label;

    SpaceTypeEnum(Integer key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * 根据 KEY 获取枚举
     *
     * @param key 状态键值
     * @return 枚举对象，未找到时返回 null
     */
    public static SpaceTypeEnum getEnumByKey(Integer key) {
        if (ObjUtil.isEmpty(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey().equals(key), values());
    }
}

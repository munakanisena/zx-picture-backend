package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Megumi
 * @description : 图片互动状态枚举
 * @createDate : 2025/6/1 下午3:44
 */
@Getter
public enum PictureInteractionStatusEnum {
    NOT_INTERACTED(0, "未互动"),
    INTERACTED(1, "已互动");

    private final Integer key;
    private final String label;

    PictureInteractionStatusEnum(Integer key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * 是否互动
     *
     * @param key 状态键值
     * @return true: 已互动，false: 未互动
     */
    public static Boolean isInteracted(Integer key) {
        return INTERACTED.getKey().equals(key);
    }

    /**
     * 获取所有枚举的 key
     *
     * @return key 列表
     */
    public static List<Integer> getKeys() {
        return Arrays.stream(values())
                .map(PictureInteractionStatusEnum::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 根据 KEY 获取枚举
     *
     * @param key 状态键值
     * @return 枚举对象，未找到时返回 null
     */
    public static PictureInteractionStatusEnum getEnumByKey(Integer key) {
        if (ObjUtil.isEmpty(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey().equals(key), values());
    }
}



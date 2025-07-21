package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Megumi
 * @description : 图片互动类型枚举
 * @createDate : 2025/6/1 下午3:44
 */
@Getter
public enum PictureInteractionTypeEnum {
    LIKE(0, "点赞"),
    COLLECTION(1, "收藏"),
    DOWNLOAD(2, "下载");

    private final Integer key;
    private final String label;

    PictureInteractionTypeEnum(int key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * 根据 KEY 获取枚举
     *
     * @param key 状态键值
     * @return 枚举对象，未找到时返回 null
     */
    public static PictureInteractionTypeEnum getEnumByKey(Integer key) {
        if (ObjUtil.isEmpty(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey().equals(key), values());
    }

    /**
     * 获取所有枚举的 key
     *
     * @return key 列表
     */
    public static List<Integer> getKeys() {
        return Arrays.stream(values())
                .map(PictureInteractionTypeEnum::getKey)
                .collect(Collectors.toList());
    }
}

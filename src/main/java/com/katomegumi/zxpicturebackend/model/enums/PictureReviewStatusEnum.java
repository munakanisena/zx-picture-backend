package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图片审核状态 枚举类
 */
@Getter
public enum PictureReviewStatusEnum {
    REVIEW(0, "待审核"),
    PASS(1, "通过"),
    REJECT(2, "拒绝");

    private final int key;
    private final String label;

    PictureReviewStatusEnum(int key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * 根据 KEY 获取枚举
     * @param key 状态键值
     * @return 枚举对象，未找到时返回 null
     */
    public static PictureReviewStatusEnum getEnumByKey(Integer key) {
        if (ObjUtil.isEmpty(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey() == key, values());
    }
}

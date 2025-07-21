package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author Megumi
 * 空间等级 枚举类
 */
@Getter
public enum SpaceLevelEnum {

    COMMON(0, "普通版", 100, 100L * 1024 * 1024),
    PROFESSIONAL(1, "专业版", 1000, 1000L * 1024 * 1024),
    FLAGSHIP(2, "旗舰版", 10000, 10000L * 1024 * 1024);

    private final int key;
    private final String label;
    private final long maxCount;
    private final long maxSize;

    SpaceLevelEnum(int key, String label, long maxCount, long maxSize) {
        this.key = key;
        this.label = label;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    /**
     * 根据 KEY 获取枚举
     *
     * @param key 状态键值
     * @return 枚举对象，未找到时返回 null
     */
    public static SpaceLevelEnum getEnumByKey(Integer key) {
        if (ObjUtil.isEmpty(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey() == key, values());
    }
}

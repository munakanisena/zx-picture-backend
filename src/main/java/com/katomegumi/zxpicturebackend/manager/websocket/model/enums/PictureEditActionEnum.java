package com.katomegumi.zxpicturebackend.manager.websocket.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author Megumi
 * @description 图片编辑操作枚举
 */
@Getter
public enum PictureEditActionEnum {

    ZOOM_IN("ZOOM_IN", "放大操作"),
    ZOOM_OUT("ZOOM_OUT", "缩小操作"),
    ROTATE_LEFT("ROTATE_LEFT", "左旋操作"),
    ROTATE_RIGHT("ROTATE_RIGHT", "右旋操作");

    private final String key;
    private final String label;

    PictureEditActionEnum(String key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PictureEditActionEnum getEnumByKey(String key) {
        if (ObjUtil.isEmpty(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey().equals(key), values());
    }
}

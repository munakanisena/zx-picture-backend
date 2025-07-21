package com.katomegumi.zxpicturebackend.model.enums;

import lombok.Getter;

/**
 * @author : Megumi
 * @description : 图片大小范围枚举
 * @createDate : 2025/6/22 下午5:30
 */
@Getter
public enum PictureSizeRangeEnum {
    LESS_THAN_1MB("<1MB", 0L, 1024 * 1024L),
    BETWEEN_1MB_10MB("1MB-10MB", 1024 * 1024L, 10 * 1024 * 1024L),
    BETWEEN_10MB_20MB("10MB-20MB", 10 * 1024 * 1024L, 20 * 1024 * 1024L),
    GREATER_THAN_20MB(">20MB", 20 * 1024 * 1024L, Long.MAX_VALUE);

    private final String label;
    private final Long start;
    private final Long end;

    PictureSizeRangeEnum(String label, Long start, Long end) {
        this.label = label;
        this.start = start;
        this.end = end;
    }
}


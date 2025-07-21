package com.katomegumi.zxpicturebackend.model.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author : Megumi
 * @description : 抓取源枚举
 * @createDate : 2025/6/6 下午4:50
 */
@Getter
public enum CaptureSourceEnum {
    //注: 第一个%s为关键词, 第二个%s为开始,第三个 %s为抓取数量
    BING("BING", "必应", "https://cn.bing.com/images/async?q=%s&mmasync=1&first=%s&count=%s");
    //暂时只有必应 后续有时间增加抓取源
//    BAIDU("BAIDU", "百度", "");

    private final String key;
    private final String label;
    private final String url;

    CaptureSourceEnum(String key, String label, String url) {
        this.key = key;
        this.label = label;
        this.url = url;
    }

    /**
     * 根据key获取枚举
     *
     * @param key key
     * @return 枚举
     */
    public static CaptureSourceEnum getEnumByKey(String key) {
        if (StrUtil.isBlank(key)) {
            return null;
        }
        return ArrayUtil.firstMatch(e -> e.getKey().equals(key), values());
    }
}


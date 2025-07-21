package com.katomegumi.zxpicturebackend.core.api.search.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 以图搜图的 结果
 *
 * @author Megumi
 */
@Data
public class SearchPictureResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 图片来源地址
     */
    private String fromUrl;

}


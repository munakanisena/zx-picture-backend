package com.katomegumi.zxpicturebackend.manager.upload.modal;

import lombok.Data;

/**
 * 解析图片 封装对象 (可直接转换picture实体类)
 * @author Megumi
 */
@Data
public class UploadPictureResult {
    /**
     * 原图片地址
     */
    private String originUrl;

    /**
     * 原图片大小 (单位: B)
     */
    private Long originSize;

    /**
     * 原图片类型
     */
    private String originFormat;

    /**
     * 原图片宽度
     */
    private int originWidth;

    /**
     * 原图片高度
     */
    private int originHeight;

    /**
     * 原图片宽高比
     */
    private Double originScale;

    /**
     * 原图片主色调
     */
    private String originColor ;

    /**
     * 原图片资源路径
     */
    private String originPath;

    /**
     * 图片名称(展示)
     */
    private String picName;

    /**
     * 压缩图片地址
     */
    private String compressUrl;

    /**
     * 压缩图资源路径
     */
    private String compressPath;

    /**
     * 压缩图片格式
     */
    private String compressFormat;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 缩略图资源路径
     */
    private String thumbnailPath;

}

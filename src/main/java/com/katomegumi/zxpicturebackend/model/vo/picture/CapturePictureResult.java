package com.katomegumi.zxpicturebackend.model.vo.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : Megumi
 * @description : 爬取图片结果
 * @createDate : 2025/6/6 下午1:07
 */
@Data
public class CapturePictureResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 爬取图片地址
     */
    private String captureUrl;

    /**
     * 图片处理地址
     */
    private String handleCaptureUrl;

    /**
     * 图片图片名称
     */
    private String pictureName;

}


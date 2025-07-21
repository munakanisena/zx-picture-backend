package com.katomegumi.zxpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户编辑更新时使用  两个类区分开来
 *
 * @author Megumi
 */
@Data
public class PictureEditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 简介
     */
    private String picDesc;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 空间id
     */
    private Long spaceId;
}


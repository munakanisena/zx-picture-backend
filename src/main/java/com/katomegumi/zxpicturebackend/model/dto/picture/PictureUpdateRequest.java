package com.katomegumi.zxpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 给管理员使用的
 */
@Data
public class PictureUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称（展示）
     */
    private String picName;

    /**
     * 图片描述（展示）
     */
    private String picDesc;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}

package com.katomegumi.zxpicturebackend.model.dto.category;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : Megumi
 * @description : 添加分类请求
 * @createDate : 2025/5/27 下午8:16
 */
@Data
public class CategoryAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类 ID（0-表示顶层分类）
     */
    private Long parentId;

}


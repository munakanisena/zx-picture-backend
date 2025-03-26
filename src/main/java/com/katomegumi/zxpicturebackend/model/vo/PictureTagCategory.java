package com.katomegumi.zxpicturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 返回给前端的标签 便于搜索
 */

@Data
public class PictureTagCategory implements Serializable {

    private static final long serialVersionUID = 701533966080572008L;

    /**
     * 标签集合
     */
    private List<String> tagList ;

    /**
     * 分类
     */
    private List<String> CategoryList;

}

package com.katomegumi.zxpicturebackend.model.vo.space.analyze;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Megumi
 * 空间图片分类分析响应
 */
@Data
@Builder
public class SpaceCategoryAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 当前图片分类名称
     */
    private String categoryName;
    /**
     * 当前分类图片数量
     */
    private Long count;
    /**
     * 当前分类图片总大小
     */
    private Long totalSize;
}

package com.katomegumi.zxpicturebackend.model.vo.category;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : Megumi
 * @description : 分类VO
 * @createDate : 2025/5/27 下午8:49
 */
@Data
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类 ID（0-表示顶层分类）
     */
    private Long parentId;

    /**
     * 使用数量
     */
    private Integer useNum;

    /**
     * 创建用户 ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;
}


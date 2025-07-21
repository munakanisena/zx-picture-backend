package com.katomegumi.zxpicturebackend.model.dto.category;

import com.katomegumi.zxpicturebackend.core.common.req.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * @author : Megumi
 * @description : 分类查询请求
 * @createDate : 2025/5/28 上午10:30
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryQueryRequest extends PageRequest implements Serializable {

    /**
     * 主键id
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


    private static final long serialVersionUID = 1L;
}


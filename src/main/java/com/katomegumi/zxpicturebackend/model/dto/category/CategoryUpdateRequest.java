package com.katomegumi.zxpicturebackend.model.dto.category;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : Megumi
 * @description : 分类更新请求
 * @createDate : 2025/5/27 下午8:34
 */
@Data
public class CategoryUpdateRequest implements Serializable {

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


    private static final long serialVersionUID = 1L;
}


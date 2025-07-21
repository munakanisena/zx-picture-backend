package com.katomegumi.zxpicturebackend.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 分类表
 *
 * @author Megfumi
 * @TableName picture_category
 */
@TableName(value = "picture_category")
@Data
public class PictureCategory implements Serializable {
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
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
     * 是否删除（0-正常, 1-删除）
     */
    private Integer isDelete;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
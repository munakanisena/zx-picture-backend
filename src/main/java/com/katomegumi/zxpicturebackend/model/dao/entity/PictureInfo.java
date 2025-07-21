package com.katomegumi.zxpicturebackend.model.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片信息表
 *
 * @author Megumi
 * @TableName picture_info
 */
@TableName(value = "picture_info")
@Data
public class PictureInfo implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 原图片地址(下载时使用)
     */
    private String originUrl;
    /**
     * 原图大小（单位: B）
     */
    private Long originSize;
    /**
     * 原图格式
     */
    private String originFormat;
    /**
     * 原图宽度
     */
    private Integer originWidth;
    /**
     * 原图高度
     */
    private Integer originHeight;
    /**
     * 原图比例（宽高比）
     */
    private Double originScale;
    /**
     * 原图主色调
     */
    private String originColor;
    /**
     * 原图片资源路径
     */
    private String originPath;
    /**
     * 图片名称（展示）
     */
    private String picName;
    /**
     * 图片描述（展示）
     */
    private String picDesc;
    /**
     * 图片地址（展示时使用, 压缩图地址）
     */
    private String compressUrl;
    /**
     * 压缩图资源路径
     */
    private String compressPath;
    /**
     * 压缩图格式
     */
    private String compressFormat;
    /**
     * 缩略图 url(可能用也可能不用)
     */
    private String thumbnailUrl;
    /**
     * 缩略图资源路径
     */
    private String thumbnailPath;
    /**
     * 分类 ID
     */
    private Long categoryId;
    /**
     * 标签（JSON 数组）
     */
    private String tags;
    /**
     * 创建用户 id
     */
    private Long userId;
    /**
     * 所属空间 ID（0-表示公共空间）
     */
    private Long spaceId;
    /**
     * 0为待审核; 1为审核通过; 2为审核失败
     */
    private Integer reviewStatus;
    /**
     * 审核信息
     */
    private String reviewMessage;
    /**
     * 审核人id
     */
    private Long reviewerId;
    /**
     * 审核时间
     */
    private Date reviewTime;
    /**
     * 点赞数量
     */
    private Integer likeQuantity;
    /**
     * 收藏数量
     */
    private Integer collectQuantity;
    /**
     * 点赞变化量
     */
    @TableField(exist = false)
    private Integer likeDelta;
    /**
     * 收藏变化量
     */
    @TableField(exist = false)
    private Integer collectDelta;

    /**
     * 分析空间使用
     */
    @TableField(exist = false)
    private String period;

    /**
     * 是否删除（0-正常, 1-删除）
     */
    @TableLogic
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
}
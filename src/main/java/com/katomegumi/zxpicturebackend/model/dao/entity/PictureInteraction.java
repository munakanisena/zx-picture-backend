package com.katomegumi.zxpicturebackend.model.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片交互表
 *
 * @author Megumi
 * @TableName picture_interaction
 */
@TableName(value = "picture_interaction")
@Data
public class PictureInteraction implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 图片 ID
     */
    private Long pictureId;

    /**
     * 交互类型（0-点赞, 1-收藏）
     */
    private Integer interactionType;

    /**
     * 交互状态（0-未交互, 1-已交互）
     */
    private Integer interactionStatus;

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

}
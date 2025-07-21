package com.katomegumi.zxpicturebackend.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间信息表
 *
 * @author Megumi
 * @TableName space_info
 */
@TableName(value = "space_info")
@Data
public class SpaceInfo implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建用户 ID
     */
    private Long userId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间类型:1-私人空间,2-团队空间
     */
    private Integer spaceType;

    /**
     * 空间级别:0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小(单位:KB)
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间下已使用的空间(单位:KB)
     */
    private Long usedSize;

    /**
     * 当前空间下已使用的的图片数量
     */
    private Long usedCount;

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
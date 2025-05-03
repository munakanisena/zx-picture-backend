package com.katomegumi.zxpicturebackend.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 团队用户关联
 * @TableName tb_space_user
 */
@TableName(value ="tb_space_user")
@Data
public class SpaceUser implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间Id
     */
    private Long spaceId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTie;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
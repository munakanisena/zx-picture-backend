package com.katomegumi.zxpicturebackend.model.vo.space.info;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : Megumi
 * @description : 用户团队空间详情
 * @createDate : 2025/6/18 下午5:27
 */
@Data
public class SpaceTeamDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 空间 ID
     */
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
     * 当前空间下图片的总大小(单位:KB)
     */
    private Long usedSize;

    /**
     * 当前空间下的图片数量
     */
    private Long usedCount;

    /**
     * 创建时间
     */
    private Date createTime;
}


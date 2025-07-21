package com.katomegumi.zxpicturebackend.model.vo.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author : Megumi
 * @description : 主页图片
 * @createDate : 2025/5/27 下午6:48
 */
@Data
public class PictureHomeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片id
     */
    private Long id;

    /**
     * 图片名称（展示）
     */
    private String picName;

    /**
     * 图片地址（展示时使用, 压缩图地址）
     */
    private String compressUrl;

    /**
     * 缩略图 url(可能用也可能不用)
     */
    private String thumbnailUrl;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 创建用户头像
     */
    private String userAvatar;

    /**
     * 所属空间 ID（0-表示公共空间）
     */
    private Long spaceId;

    /**
     * 点赞数量
     */
    private Integer likeQuantity;

    /**
     * 收藏数量
     */
    private Integer collectQuantity;

    /**
     * 是否点赞 (默认false)
     */
    private Boolean isLike = false;

    /**
     * 是否收藏 (默认false)
     */
    private Boolean isCollect = false;
}


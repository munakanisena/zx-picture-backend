package com.katomegumi.zxpicturebackend.model.vo.picture;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author : Megumi
 * @description : 用户获取图片详情
 * @createDate : 2025/5/27 下午12:11
 */
@Data
public class PictureDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片id
     */
    private Long id;

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
     * 压缩图格式
     */
    private String compressFormat;

    /**
     * 缩略图 url(可能用也可能不用)
     */
    private String thumbnailUrl;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

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
     * 0为待审核; 1为审核通过; 2为审核失败
     */
    private Integer reviewStatus;

    /**
     * 点赞数量
     */
    private Integer likeQuantity;

    /**
     * 收藏数量
     */
    private Integer collectQuantity;

    /**
     * 是否点赞 默认(false)
     */
    private Boolean isLike = false;

    /**
     * 是否收藏 默认(false)
     */
    private Boolean isCollect = false;

    /**
     * 创建时间
     */
    private Date createTime;

    public static PictureDetailVO objToVo(PictureInfo pictureInfo) {
        if (pictureInfo == null) {
            return null;
        }
        PictureDetailVO pictureDetailVO = BeanUtil.copyProperties(pictureInfo, PictureDetailVO.class);
        // 类型不同，需要转换
        pictureDetailVO.setTags(JSONUtil.toList(pictureInfo.getTags(), String.class));
        return pictureDetailVO;
    }
}


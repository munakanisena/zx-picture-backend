package com.katomegumi.zxpicturebackend.model.dto.picture;

import com.katomegumi.zxpicturebackend.core.common.req.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索词（同时搜 名称 简介 标签）
     */
    private String searchText;

    /**
     * id
     */
    private Long id;

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
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 创建用户 ID
     */
    private Long userId;

    /**
     * 所属空间 ID（0-表示公共空间）
     */
    private Long spaceId;

    /**
     * 审核状态（0-待审核, 1-通过, 2-拒绝）
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    private Long reviewerId;

    /**
     * 编辑时间[开始时间]
     */
    private String startEditTime;

    /**
     * 编辑时间[结束时间]
     */
    private String endEditTime;
}


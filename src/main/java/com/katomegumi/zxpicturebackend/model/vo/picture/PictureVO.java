package com.katomegumi.zxpicturebackend.model.vo.picture;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureCategory;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Megumi
 * @description 图片VO 管理员使用
 */
@Data
public class PictureVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
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
     * 分类信息
     */
    private PictureCategory pictureCategory;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

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


    /**
     * 封装类转对象
     */
    public static PictureInfo voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        PictureInfo pictureInfo = BeanUtil.copyProperties(pictureVO, PictureInfo.class);
        pictureInfo.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return pictureInfo;
    }

    /**
     * 对象转封装类
     */
    public static PictureVO objToVo(PictureInfo pictureInfo) {
        if (pictureInfo == null) {
            return null;
        }
        PictureVO pictureVO = BeanUtil.copyProperties(pictureInfo, PictureVO.class);
        // 类型不同，需要转换
        pictureVO.setTags(JSONUtil.toList(pictureInfo.getTags(), String.class));
        return pictureVO;
    }
}

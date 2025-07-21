package com.katomegumi.zxpicturebackend.model.vo.picture;

import lombok.Data;

/**
 * @author : Megumi
 * @description : 用户图片统计信息
 * @createDate : 2025/7/18 下午6:34
 */
@Data
public class UserPictureStatsVO {
    /**
     * 上传图片总数
     */
    private Long uploadCount;

    /**
     * 收藏图片总数
     */
    private Long collectCount;
}


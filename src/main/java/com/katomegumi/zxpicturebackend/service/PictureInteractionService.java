package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInteraction;
import com.katomegumi.zxpicturebackend.model.dto.picture.PictureInteractionRequest;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【picture_interaction(图片交互表)】的数据库操作Service
 * @createDate 2025-05-27 18:32:56
 */
public interface PictureInteractionService extends IService<PictureInteraction> {

    /**
     * 获取用户交互数据(单个图片)
     *
     * @param pictureId 图片id
     * @param userId    用户id
     * @return 用户交互数据
     */
    List<PictureInteraction> getPictureInteractionListByPictureIdAndUserId(Long pictureId, Long userId);

    /**
     * 获取用户交互数据(多个图片)
     *
     * @param pictureIds 图片列表id
     * @param userId     用户id
     * @return 用户交互数据
     */
    List<PictureInteraction> getPictureInteractionListByPictureIdsAndUserId(List<Long> pictureIds, Long userId);

    /**
     * 获取用户收藏的图片 ID 集合
     *
     * @param userId 用户 ID
     * @return 图片 ID 集合
     */
    List<Long> getCollectedPictureIds(Long userId);

    /**
     * 点赞或者收藏
     *
     * @param pictureInteractionRequest 用户交互请求
     */
    void changePictureLikeOrCollection(PictureInteractionRequest pictureInteractionRequest);
}

package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.constant.CacheConstant;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.manager.cache.HomeCategoryCacheManager;
import com.katomegumi.zxpicturebackend.manager.cache.HomePictureCacheManager;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInteraction;
import com.katomegumi.zxpicturebackend.model.dto.picture.PictureQueryRequest;
import com.katomegumi.zxpicturebackend.model.enums.PictureInteractionStatusEnum;
import com.katomegumi.zxpicturebackend.model.enums.PictureInteractionTypeEnum;
import com.katomegumi.zxpicturebackend.model.enums.PictureReviewStatusEnum;
import com.katomegumi.zxpicturebackend.model.enums.SpaceTypeEnum;
import com.katomegumi.zxpicturebackend.model.vo.category.CategoryVO;
import com.katomegumi.zxpicturebackend.model.vo.picture.PictureHomeVO;
import com.katomegumi.zxpicturebackend.service.HomeService;
import com.katomegumi.zxpicturebackend.service.PictureInteractionService;
import com.katomegumi.zxpicturebackend.service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : Megumi
 * @description :首页模块 服务实现
 * @createDate : 2025/5/28 下午1:19
 */
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomePictureCacheManager homePictureCacheManager;

    private final HomeCategoryCacheManager homeCategoryCacheManager;

    private final PictureInteractionService pictureInteractionService;

    private final PictureService pictureService;


    @Override
    public PageVO<PictureHomeVO> pageHomePictures(PictureQueryRequest pictureQueryRequest) {

        //一定只能查公共图库和未审核图片 因此在这里写死
        pictureQueryRequest.setSpaceId((long) SpaceTypeEnum.PUBLIC.getKey());
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getKey());

        LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = pictureService.getLambdaQueryWrapper(pictureQueryRequest);
        //默认创建时间排序 todo 后续考虑使用评分
        lambdaQueryWrapper.orderByDesc(PictureInfo::getCreateTime);
        //获取首页图片列表
        PageVO<PictureHomeVO> pictureHomeVOPageVO = homePictureCacheManager.pageHomePictures(lambdaQueryWrapper, pictureQueryRequest);

        //填充图片信息
        List<PictureHomeVO> pictureHomeVOList = pictureHomeVOPageVO.getRecords();
        //喜欢和收藏 Map
        HashMap<Long, Boolean> likeMap = new HashMap<>();
        HashMap<Long, Boolean> collectionMap = new HashMap<>();

        //用户登录，返回交互的数据
        if (CollUtil.isNotEmpty(pictureHomeVOList) && StpKit.USER.isLogin()) {
            List<Long> pictureIds = pictureHomeVOList.stream().map(PictureHomeVO::getId).collect(Collectors.toList());
            List<PictureInteraction> pictureInteractionList = pictureInteractionService.getPictureInteractionListByPictureIdsAndUserId(pictureIds, StpKit.USER.getLoginIdAsLong());

            if (CollUtil.isNotEmpty(pictureInteractionList)) {
                //填充map
                pictureInteractionList.forEach(pi -> {
                    if (PictureInteractionTypeEnum.LIKE.getKey().equals(pi.getInteractionType())) {
                        likeMap.put(pi.getPictureId(), PictureInteractionStatusEnum.isInteracted(pi.getInteractionStatus()));
                    }

                    if (PictureInteractionTypeEnum.COLLECTION.getKey().equals(pi.getInteractionType())) {
                        collectionMap.put(pi.getPictureId(), PictureInteractionStatusEnum.isInteracted(pi.getInteractionStatus()));
                    }
                });
            }
        }
        //获取所有图片ID
        List<Long> pictureInfoIds = pictureHomeVOList.stream()
                .map(PictureHomeVO::getId)
                .collect(Collectors.toList());

        //添加点赞的变化量
        Map<Long, Integer> likeDeltas = pictureService.getRedisDeltas(CacheConstant.PICTURE.PICTURE_INTERACTION_LIKE_KEY_PREFIX, pictureInfoIds);
        Map<Long, Integer> collectDeltas = pictureService.getRedisDeltas(CacheConstant.PICTURE.PICTURE_INTERACTION_COLLECTION_KEY_PREFIX, pictureInfoIds);

        // 设置图片实时数据  用户交互状态
        pictureHomeVOList.forEach(pv -> {
            Long id = pv.getId();
            // 设置用户交互状态
            pv.setIsLike(likeMap.getOrDefault(id, false));
            pv.setIsCollect(collectionMap.getOrDefault(id, false));
            // 添加Redis中的变化量到基础数据
            pv.setLikeQuantity(pv.getLikeQuantity() + likeDeltas.getOrDefault(id, 0));
            pv.setCollectQuantity(pv.getCollectQuantity() + collectDeltas.getOrDefault(id, 0));
        });
        pictureHomeVOPageVO.setRecords(pictureHomeVOList);

        return pictureHomeVOPageVO;

    }


    @Override
    public List<CategoryVO> listHomeCategories() {
        return homeCategoryCacheManager.listHomeCategories();
    }

}


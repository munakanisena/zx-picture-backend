package com.katomegumi.zxpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInteraction;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureInteractionMapper;
import com.katomegumi.zxpicturebackend.model.dto.picture.PictureInteractionRequest;
import com.katomegumi.zxpicturebackend.model.enums.PictureInteractionStatusEnum;
import com.katomegumi.zxpicturebackend.model.enums.PictureInteractionTypeEnum;
import com.katomegumi.zxpicturebackend.service.PictureInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【picture_interaction(图片交互表)】的数据库操作Service实现
 * @createDate 2025-05-27 18:32:56
 */
@Service
@RequiredArgsConstructor
public class PictureInteractionServiceImpl extends ServiceImpl<PictureInteractionMapper, PictureInteraction>
        implements PictureInteractionService {

    @Override
    public List<PictureInteraction> getPictureInteractionListByPictureIdAndUserId(Long pictureId, Long userId) {
        LambdaQueryWrapper<PictureInteraction> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(PictureInteraction::getPictureId, pictureId)
                .eq(PictureInteraction::getUserId, userId);

        return Optional.ofNullable(this.list(lambdaQueryWrapper))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<PictureInteraction> getPictureInteractionListByPictureIdsAndUserId(List<Long> pictureIds, Long userId) {
        LambdaQueryWrapper<PictureInteraction> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(PictureInteraction::getPictureId, pictureIds)
                .eq(PictureInteraction::getUserId, userId);

        return Optional.ofNullable(this.list(lambdaQueryWrapper))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Long> getCollectedPictureIds(Long userId) {
        List<PictureInteraction> pictureInteractionList = lambdaQuery()
                .select(PictureInteraction::getPictureId)
                .eq(PictureInteraction::getUserId, userId)
                .eq(PictureInteraction::getInteractionType, PictureInteractionTypeEnum.COLLECTION.getKey())
                .eq(PictureInteraction::getInteractionStatus, PictureInteractionStatusEnum.INTERACTED.getKey())
                .list();
        return pictureInteractionList.stream().map(PictureInteraction::getPictureId).collect(Collectors.toList());
    }

    @Override
    public void changePictureLikeOrCollection(PictureInteractionRequest pictureInteractionRequest) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        Long pictureId = pictureInteractionRequest.getId();
        Integer interactionType = pictureInteractionRequest.getInteractionType();
        Integer interactionStatus = pictureInteractionRequest.getInteractionStatus();

        //查询是否已经存在交互
        LambdaQueryWrapper<PictureInteraction> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(PictureInteraction::getPictureId, pictureId)
                .eq(PictureInteraction::getUserId, userId)
                .eq(PictureInteraction::getInteractionType, interactionType);

        PictureInteraction pictureInteraction = this.getOne(lambdaQueryWrapper);
        if (pictureInteraction == null) {
            //不存在 此时必须只能设置 互动状态
            if (!PictureInteractionStatusEnum.INTERACTED.getKey().equals(interactionStatus)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "首次只允许为互动状态");
            }
            pictureInteraction = new PictureInteraction();
            pictureInteraction.setPictureId(pictureId);
            pictureInteraction.setUserId(userId);
            pictureInteraction.setInteractionType(interactionType);
            pictureInteraction.setInteractionStatus(interactionStatus);
            boolean result = this.save(pictureInteraction);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        } else {

            if (pictureInteraction.getInteractionStatus().equals(interactionStatus)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不允许重复点赞或者重复收藏操作");
            }

            //存在 修改交互状态 (交互类型不变)
            pictureInteraction.setInteractionStatus(interactionStatus);
//            boolean result = this.updateById(pictureInteraction); 联合主键
            boolean result = this.update(new LambdaUpdateWrapper<PictureInteraction>()
                    .eq(PictureInteraction::getPictureId, pictureId)
                    .eq(PictureInteraction::getUserId, userId)
                    .eq(PictureInteraction::getInteractionType, interactionType)
                    .set(PictureInteraction::getInteractionStatus, interactionStatus)
            );
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        }
    }
}





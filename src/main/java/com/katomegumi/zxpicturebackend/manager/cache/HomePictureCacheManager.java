package com.katomegumi.zxpicturebackend.manager.cache;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.constant.CacheConstant;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureInfoMapper;
import com.katomegumi.zxpicturebackend.model.dao.mapper.UserInfoMapper;
import com.katomegumi.zxpicturebackend.model.dto.picture.PictureQueryRequest;
import com.katomegumi.zxpicturebackend.model.vo.picture.PictureHomeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author : Megumi
 * @description : 首页图片 缓存管理类
 * @createDate : 2025/5/28 下午5:28
 */
@Component
@RequiredArgsConstructor
public class HomePictureCacheManager {

    private final PictureInfoMapper pictureInfoMapper;

    private final UserInfoMapper userInfoMapper;

    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_MANAGER
            , value = CacheConstant.HOME_PICTURE_CACHE_NAME,
            key = "(#pictureQueryRequest.categoryId != null ? #pictureQueryRequest.categoryId + '::' : '' )+ " +
                    "(#pictureQueryRequest.searchText != null ? #pictureQueryRequest.searchText + '::' : '' )+ " +
                    "(#pictureQueryRequest.current + '-' + #pictureQueryRequest.pageSize)"
    )
    public PageVO<PictureHomeVO> pageHomePictures(LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper, PictureQueryRequest pictureQueryRequest) {
        Page<PictureInfo> picturePage = pictureInfoMapper.selectPage(pictureQueryRequest.getPage(PictureInfo.class), lambdaQueryWrapper);
        PageVO<PictureHomeVO> pictureHomeVOPageVO = new PageVO<>(
                picturePage.getCurrent(),
                picturePage.getSize(),
                picturePage.getTotal(),
                picturePage.getPages(),
                Optional.ofNullable(picturePage.getRecords())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(pictureInfo -> BeanUtil.copyProperties(pictureInfo, PictureHomeVO.class))
                        .collect(Collectors.toList())
        );
        if (CollUtil.isNotEmpty(pictureHomeVOPageVO.getRecords())) {
            //获取用户信息
            Set<Long> userIds = picturePage.getRecords().stream().map(PictureInfo::getUserId).collect(Collectors.toSet());
            Map<Long, UserInfo> userInfoMap = userInfoMapper.selectMapByIds(userIds);

            //补充作者信息
            pictureHomeVOPageVO.getRecords().forEach(p -> {
                Long userId = p.getUserId();
                if (userInfoMap.containsKey(userId)) {
                    UserInfo userInfo = userInfoMap.get(userId);
                    p.setUserName(userInfo.getName());
                    p.setUserAvatar(userInfo.getAvatar());
                }
            });
        }

        return pictureHomeVOPageVO;
    }
}



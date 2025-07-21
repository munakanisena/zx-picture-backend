package com.katomegumi.zxpicturebackend.manager.task;

import com.katomegumi.zxpicturebackend.core.constant.CacheConstant;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : Megumi
 * @description : redis 同步到数据库任务
 * @createDate : 2025/6/3 下午8:29
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisToDbSyncTask {

    private final PictureInfoMapper pictureInfoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    //可以考虑lu脚本保证原子性
    @Transactional
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncLikesAndCollectionToDb() {
        // 获取所有变化的数据（包括点赞和收藏）
        Map<Object, Object> likeMap = stringRedisTemplate.opsForHash().entries(CacheConstant.PICTURE.PICTURE_INTERACTION_LIKE_KEY_PREFIX);
        Map<Object, Object> collectionMap = stringRedisTemplate.opsForHash().entries(CacheConstant.PICTURE.PICTURE_INTERACTION_COLLECTION_KEY_PREFIX);

        // 合并所有需要更新的图片ID（取并集）
        Set<Long> allPictureInfoIds = new HashSet<>();
        likeMap.keySet().forEach(key -> allPictureInfoIds.add(Long.parseLong(key.toString())));
        collectionMap.keySet().forEach(key -> allPictureInfoIds.add(Long.parseLong(key.toString())));

        // 构建增量更新对象
        List<PictureInfo> pictureInfoList = allPictureInfoIds.stream().map(id -> {
            PictureInfo pictureInfo = new PictureInfo();
            pictureInfo.setId(id);

            // 安全获取点赞增量（处理null）
            Object likeDelta = likeMap.get(id.toString());
            pictureInfo.setLikeDelta(likeDelta != null ? Integer.parseInt(likeDelta.toString()) : 0);

            // 安全获取收藏增量（处理null）
            Object collectDelta = collectionMap.get(id.toString());
            pictureInfo.setCollectDelta(collectDelta != null ? Integer.parseInt(collectDelta.toString()) : 0);

            return pictureInfo;
        }).collect(Collectors.toList());

        // 批量增量更新数据库
        if (!pictureInfoList.isEmpty()) {
            pictureInfoMapper.batchIncrementLikesAndCollects(pictureInfoList);

            // 清空Redis计数（成功更新后）
            stringRedisTemplate.delete(CacheConstant.PICTURE.PICTURE_INTERACTION_LIKE_KEY_PREFIX);
            stringRedisTemplate.delete(CacheConstant.PICTURE.PICTURE_INTERACTION_COLLECTION_KEY_PREFIX);
        }

    }
}


package com.katomegumi.zxpicturebackend.manager.cache;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.katomegumi.zxpicturebackend.core.constant.CacheConstant;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureCategory;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureCategoryMapper;
import com.katomegumi.zxpicturebackend.model.vo.category.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author : Megumi
 * @description : 首页 分类缓存管理
 * @createDate : 2025/5/28 下午5:46
 */
@Component
@RequiredArgsConstructor
public class HomeCategoryCacheManager {

    private final PictureCategoryMapper pictureCategoryMapper;

    @Cacheable(cacheManager = CacheConstant.CAFFEINE_CACHE_MANAGER, value = CacheConstant.HOME_CATEGORY_CACHE_NAME)
    public List<CategoryVO> listHomeCategories() {
        LambdaQueryWrapper<PictureCategory> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //获取顶层分类即可
        lambdaQueryWrapper.eq(PictureCategory::getParentId, 0);
        List<PictureCategory> categoryList = this.pictureCategoryMapper.selectList(lambdaQueryWrapper);

        return Optional.ofNullable(categoryList)
                .orElse(Collections.emptyList())
                .stream()
                .map(category -> BeanUtil.copyProperties(category, CategoryVO.class))
                .collect(Collectors.toList());
    }
}


package com.katomegumi.zxpicturebackend.model.dao.mapper;

import com.katomegumi.zxpicturebackend.model.dao.entity.PictureCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.service.PictureCategoryService;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Map;

/**
 * @author Megumi
 * @description 针对表【picture_category(分类表)】的数据库操作Mapper
 * @createDate 2025-05-27 20:15:27
 * @Entity com.katomegumi.zxpicturebackend.model.dao.entity.PictureCategory
 */
public interface PictureCategoryMapper extends BaseMapper<PictureCategory> {

    /**
     * 根据 ID 集合查询分类，并以 ID 为键构建 Map
     *
     * @param categoryIds 分类 ID 集合
     * @return Map<分类ID, 分类实体>
     */
    @MapKey("id")
    Map<Long, PictureCategory> selectMapByIds(@Param("categoryIds") Collection<Long> categoryIds);
}





package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureCategory;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dto.category.CategoryAddRequest;
import com.katomegumi.zxpicturebackend.model.dto.category.CategoryQueryRequest;
import com.katomegumi.zxpicturebackend.model.dto.category.CategoryUpdateRequest;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.model.vo.category.CategoryVO;

import java.util.Map;
import java.util.Set;

/**
 * @author lirui
 * @description 针对表【picture_category(分类表)】的数据库操作Service
 * @createDate 2025-05-27 20:15:28
 */
public interface PictureCategoryService extends IService<PictureCategory> {

    /**
     * 添加分类
     *
     * @param categoryAddRequest 分类信息
     */
    void addCategory(CategoryAddRequest categoryAddRequest);

    /**
     * 删除分类
     *
     * @param categoryId 分类id
     */
    void deleteCategory(Long categoryId);

    /**
     * 更新用户
     *
     * @param categoryUpdateRequest 类信息
     */
    void updateCategory(CategoryUpdateRequest categoryUpdateRequest);

    /**
     * 获取分类管理页面列表
     *
     * @param categoryQueryRequest 分类查询信息
     * @return 分类管理列表
     */
    PageVO<CategoryVO> getCategoryPageListAsManage(CategoryQueryRequest categoryQueryRequest);

    /**
     * 根据分类id获取 分类映射信息
     *
     * @param categoryIds 分类id集合
     * @return 用户Id->用户信息
     */
    Map<Long, PictureCategory> selectMapByIds(Set<Long> categoryIds);

    /**
     * 根据图片信息更新分类
     *
     * @param categoryId     分类id
     * @param oldPictureInfo 旧图片信息
     */
    void updateCategoryByPictureInfo(Long categoryId, PictureInfo oldPictureInfo);
}

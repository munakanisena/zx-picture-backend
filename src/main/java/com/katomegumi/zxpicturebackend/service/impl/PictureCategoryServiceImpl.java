package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.util.SFunctionUtils;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureCategory;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureCategoryMapper;
import com.katomegumi.zxpicturebackend.model.dto.category.CategoryAddRequest;
import com.katomegumi.zxpicturebackend.model.dto.category.CategoryQueryRequest;
import com.katomegumi.zxpicturebackend.model.dto.category.CategoryUpdateRequest;
import com.katomegumi.zxpicturebackend.model.vo.category.CategoryVO;
import com.katomegumi.zxpicturebackend.service.PictureCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author lirui
 * @description 针对表【picture_category(分类表)】的数据库操作Service实现
 * @createDate 2025-05-27 20:15:27
 */
@Service
@RequiredArgsConstructor
public class PictureCategoryServiceImpl extends ServiceImpl<PictureCategoryMapper, PictureCategory>
        implements PictureCategoryService {

    private final PictureCategoryMapper pictureCategoryMapper;

    @Override
    public void addCategory(CategoryAddRequest categoryAddRequest) {
        PictureCategory pictureCategory = BeanUtil.copyProperties(categoryAddRequest, PictureCategory.class);
        pictureCategory.setUserId(StpKit.USER.getLoginIdAsLong());
        boolean result = this.save(pictureCategory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "添加失败");
    }

    @Override
    public void deleteCategory(Long categoryId) {
        boolean result = this.removeById(categoryId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");
    }

    @Override
    public void updateCategory(CategoryUpdateRequest categoryUpdateRequest) {
        PictureCategory pictureCategory = BeanUtil.copyProperties(categoryUpdateRequest, PictureCategory.class);
        boolean result = this.updateById(pictureCategory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
    }

    @Override
    public PageVO<CategoryVO> getCategoryPageListAsManage(CategoryQueryRequest categoryQueryRequest) {
        LambdaQueryWrapper<PictureCategory> lambdaQueryWrapper = this.getLambdaQueryWrapper(categoryQueryRequest);
        Page<PictureCategory> pictureCategoryPage = this.page(categoryQueryRequest.getPage(PictureCategory.class), lambdaQueryWrapper);
        return new PageVO<>(
                pictureCategoryPage.getCurrent(),
                pictureCategoryPage.getSize(),
                pictureCategoryPage.getTotal(),
                pictureCategoryPage.getPages(),
                Optional.ofNullable(pictureCategoryPage.getRecords())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(pictureCategory -> BeanUtil.copyProperties(pictureCategory, CategoryVO.class))
                        .collect(Collectors.toList())
        );
    }

    /**
     * 构造查询参数
     *
     * @param categoryQueryRequest 分类查询信息
     * @return 查询参数
     */
    private LambdaQueryWrapper<PictureCategory> getLambdaQueryWrapper(CategoryQueryRequest categoryQueryRequest) {
        LambdaQueryWrapper<PictureCategory> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Long id = categoryQueryRequest.getId();
        String name = categoryQueryRequest.getName();
        Long parentId = categoryQueryRequest.getParentId();
        Integer useNum = categoryQueryRequest.getUseNum();
        Long userId = categoryQueryRequest.getUserId();
        String sortField = categoryQueryRequest.getSortField();
        Boolean sortOrder = categoryQueryRequest.getSortOrder();
        lambdaQueryWrapper
                .eq(ObjUtil.isNotEmpty(id), PictureCategory::getId, id)
                .like(StrUtil.isNotEmpty(name), PictureCategory::getName, name)
                .eq(ObjUtil.isNotEmpty(parentId), PictureCategory::getParentId, parentId)
                .eq(ObjUtil.isNotEmpty(useNum), PictureCategory::getUseNum, useNum)
                .eq(ObjUtil.isNotEmpty(userId), PictureCategory::getUserId, userId);

        //构造排序
        if (sortField != null) {
            lambdaQueryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder, SFunctionUtils.getSFunction(PictureCategory.class, sortField));
        } else {
            lambdaQueryWrapper.orderByDesc(PictureCategory::getCreateTime);
        }
        return lambdaQueryWrapper;
    }

    @Override
    public Map<Long, PictureCategory> selectMapByIds(Set<Long> categoryIds) {
        return pictureCategoryMapper.selectMapByIds(categoryIds);
    }

    @Override
    public void updateCategoryByPictureInfo(Long categoryId, PictureInfo oldPictureInfo) {
        Long oldCategoryId = oldPictureInfo.getCategoryId();
        if (oldCategoryId == null) {
            //表示是新增
            boolean result = this.update(new LambdaUpdateWrapper<PictureCategory>()
                    .setIncrBy(PictureCategory::getUseNum, 1)
                    .eq(PictureCategory::getId, categoryId));
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            return;
        }
        if (oldCategoryId.equals(categoryId)) {
            return;
        }

        //表示是修改 减去
        boolean result = this.update(new LambdaUpdateWrapper<PictureCategory>()
                .setIncrBy(PictureCategory::getUseNum, -1)
                .eq(PictureCategory::getId, oldCategoryId));
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        //新增
        result = this.update(new LambdaUpdateWrapper<PictureCategory>()
                .setIncrBy(PictureCategory::getUseNum, 1)
                .eq(PictureCategory::getId, categoryId));
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }
}



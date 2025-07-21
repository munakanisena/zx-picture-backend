package com.katomegumi.zxpicturebackend.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.katomegumi.zxpicturebackend.core.annotation.AuthCheck;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.req.DeleteRequest;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import com.katomegumi.zxpicturebackend.core.constant.ApiRouterConstant;
import com.katomegumi.zxpicturebackend.core.constant.UserConstant;
import com.katomegumi.zxpicturebackend.model.dto.category.CategoryAddRequest;
import com.katomegumi.zxpicturebackend.model.dto.category.CategoryQueryRequest;
import com.katomegumi.zxpicturebackend.model.dto.category.CategoryUpdateRequest;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.model.vo.category.CategoryVO;
import com.katomegumi.zxpicturebackend.service.PictureCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Megumi
 * @description : 图片分类接口(只准管理员操作)
 */
@RestController
@RequestMapping(ApiRouterConstant.API_PICTURE_CATEGORY_URL_PREFIX)
@RequiredArgsConstructor
public class CategoryController {

    private final PictureCategoryService pictureCategoryService;

    /**
     * 添加分类
     *
     * @param categoryAddRequest 分类添加请求
     * @return 添加结果
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addCategory(@RequestBody CategoryAddRequest categoryAddRequest) {
        ThrowUtils.throwIf(categoryAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isEmpty(categoryAddRequest.getName()), ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        pictureCategoryService.addCategory(categoryAddRequest);
        return ResultUtils.success();
    }

    /**
     * 删除分类
     *
     * @param deleteRequest 分类删除请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCategory(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long categoryId = deleteRequest.getId();
        ThrowUtils.throwIf(categoryId == null || categoryId < 0, ErrorCode.PARAMS_ERROR);
        pictureCategoryService.deleteCategory(categoryId);
        return ResultUtils.success();
    }


    /**
     * 更新分类
     *
     * @param categoryUpdateRequest 分类更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCategory(@RequestBody CategoryUpdateRequest categoryUpdateRequest) {
        ThrowUtils.throwIf(categoryUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(categoryUpdateRequest.getId()) || categoryUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isEmpty(categoryUpdateRequest.getName()), ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        pictureCategoryService.updateCategory(categoryUpdateRequest);
        return ResultUtils.success();
    }

    /**
     * 获取管理页面分类列表
     *
     * @param categoryQueryRequest 分类查询请求
     * @return 管理页面分类列表
     */
    @PostMapping("/manage/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PageVO<CategoryVO>> getCategoryPageListAsManage(@RequestBody CategoryQueryRequest categoryQueryRequest) {
        ThrowUtils.throwIf(categoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureCategoryService.getCategoryPageListAsManage(categoryQueryRequest));
    }
}


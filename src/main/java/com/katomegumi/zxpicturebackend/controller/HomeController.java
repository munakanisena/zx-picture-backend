package com.katomegumi.zxpicturebackend.controller;

import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import com.katomegumi.zxpicturebackend.core.constant.ApiRouterConstant;
import com.katomegumi.zxpicturebackend.model.dto.picture.PictureQueryRequest;
import com.katomegumi.zxpicturebackend.model.vo.category.CategoryVO;
import com.katomegumi.zxpicturebackend.model.vo.picture.PictureHomeVO;
import com.katomegumi.zxpicturebackend.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author : Megumi
 * @description : 首页模块
 * @createDate : 2025/5/28 下午1:16
 */
@RestController
@RequestMapping(ApiRouterConstant.API_HOME_URL_PREFIX)
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 获取首页图片
     *
     * @param pictureQueryRequest 查询参数
     * @return 图片列表
     */
    @PostMapping("/pictures")
    public BaseResponse<PageVO<PictureHomeVO>> pageHomePictures(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        return ResultUtils.success(homeService.pageHomePictures(pictureQueryRequest));
    }

    /**
     * 获取首页 分类列表
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public BaseResponse<List<CategoryVO>> listHomeCategories() {
        return ResultUtils.success(homeService.listHomeCategories());
    }

}


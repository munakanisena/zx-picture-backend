package com.katomegumi.zxpicturebackend.controller;

import com.katomegumi.zxpicturebackend.core.annotation.AuthCheck;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import com.katomegumi.zxpicturebackend.core.constant.ApiRouterConstant;
import com.katomegumi.zxpicturebackend.core.constant.UserConstant;
import com.katomegumi.zxpicturebackend.model.dto.space.analyze.*;
import com.katomegumi.zxpicturebackend.model.vo.space.analyze.*;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceVO;
import com.katomegumi.zxpicturebackend.service.SpaceAnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Megumi
 * @description 图库分析接口(仅管理员分析)
 */
@RestController
@RequestMapping(ApiRouterConstant.API_SPACE_ANALYZE_URL_PREFIX)
@RequiredArgsConstructor
public class SpaceAnalyzeController {

    private final SpaceAnalyzeService spaceAnalyzeService;

    /**
     * 分析空间内图片使用情况
     *
     * @param spaceUsageAnalyzeRequest 空间使用分析请求
     * @return 空间使用情况
     */
    @PostMapping("/picture-usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> analyzeSpaceUsed(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceAnalyzeService.analyzeSpaceUsed(spaceUsageAnalyzeRequest));
    }


    /**
     * 分析空间内图片分类 的数量和大小
     *
     * @param spaceCategoryAnalyzeRequest 空间分类分析请求
     * @return 空间分类分析结果
     */
    @PostMapping("/picture-categories")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> analyzeSpaceCategory(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceAnalyzeService.analyzeSpaceCategory(spaceCategoryAnalyzeRequest));
    }

    /**
     * 分析空间内图片的标签出现次数
     *
     * @param spaceTagAnalyzeRequest 空间内图片标签分析请求
     * @return 空间内图片标签分析结果
     */
    @PostMapping("/picture-tags")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> analyzeSpaceTags(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceAnalyzeService.analyzeSpaceTags(spaceTagAnalyzeRequest));
    }

    /**
     * 分析空间内图片的大小范围
     *
     * @param spaceSizeAnalyzeRequest 空间图片大小范围分析请求
     * @return 空间内图片大小范围分析结果
     */
    @PostMapping("/picture-size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> analyzeSpaceSize(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceAnalyzeService.analyzeSpaceSize(spaceSizeAnalyzeRequest));
    }

    /**
     * 分析用户空间图片上传行为
     *
     * @param spaceUserAnalyzeRequest 用户空间图片上传行为分析请求
     * @return 用户空间图片上传行为分析结果
     */
    @PostMapping("/user-action")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> analyzeSpaceUserAction(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceAnalyzeService.analyzeSpaceUserAction(spaceUserAnalyzeRequest));
    }

    //---------------仅管理员使用--------------

    /**
     * 分析存储使用量 进行排名 获取排名前10的空间列表
     *
     * @return 空间排名分析结果
     */
    @GetMapping("/picture-rank")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<SpaceVO>> analyzeSpaceRank() {
        return ResultUtils.success(spaceAnalyzeService.analyzeSpaceRank());
    }

}

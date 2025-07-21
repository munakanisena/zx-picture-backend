package com.katomegumi.zxpicturebackend.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.katomegumi.zxpicturebackend.core.annotation.AuthCheck;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.core.api.search.model.SearchPictureResult;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.req.DeleteRequest;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import com.katomegumi.zxpicturebackend.core.constant.ApiRouterConstant;
import com.katomegumi.zxpicturebackend.core.constant.UserConstant;
import com.katomegumi.zxpicturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.katomegumi.zxpicturebackend.manager.auth.annotation.SaUserCheckLogin;
import com.katomegumi.zxpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.katomegumi.zxpicturebackend.model.dto.picture.*;
import com.katomegumi.zxpicturebackend.model.enums.PictureInteractionStatusEnum;
import com.katomegumi.zxpicturebackend.model.enums.PictureInteractionTypeEnum;
import com.katomegumi.zxpicturebackend.model.enums.SpaceTypeEnum;
import com.katomegumi.zxpicturebackend.model.vo.picture.*;
import com.katomegumi.zxpicturebackend.service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Megumi
 * @description 图片模块
 */
@RestController
@RequestMapping(ApiRouterConstant.API_PICTURE_URL_PREFIX)
@RequiredArgsConstructor
public class PictureController {

    private final PictureService pictureService;

    /**
     * 上传图片(可重新上传)至公共空间
     *
     * @param multipartFile        文件
     * @param pictureUploadRequest 请求
     * @return 图片详情
     */
    @PostMapping("/upload-public/file")
    @SaUserCheckLogin
    public BaseResponse<PictureDetailVO> uploadPictureByFileToPublic(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR);
        pictureUploadRequest.setSpaceId(Long.valueOf(SpaceTypeEnum.PUBLIC.getKey()));
        return ResultUtils.success(pictureService.uploadPicture(multipartFile, pictureUploadRequest));
    }

    /**
     * 根据地址上传图片(可重新上传)至公共空间
     *
     * @param pictureUploadRequest 请求
     * @return 图片详情
     */
    @PostMapping("/upload-public/url")
    @SaUserCheckLogin
    public BaseResponse<PictureDetailVO> uploadPictureByUrlToPublic(@RequestBody PictureUploadRequest pictureUploadRequest) {
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR);
        pictureUploadRequest.setSpaceId(Long.valueOf(SpaceTypeEnum.PUBLIC.getKey()));
        return ResultUtils.success(pictureService.uploadPicture(pictureUploadRequest.getPictureUrl(), pictureUploadRequest));
    }

    /**
     * 上传图片(可重新上传)至私有空间
     *
     * @param multipartFile        文件
     * @param pictureUploadRequest 请求
     * @return 图片详情
     */
    @PostMapping("/upload-space/file")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureDetailVO> uploadPictureByFileToSpace(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.uploadPicture(multipartFile, pictureUploadRequest));
    }


    /**
     * 根据id删除图片
     *
     * @param deleteRequest 删除id
     * @return 返回结果
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePictureById(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = deleteRequest.getId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        pictureService.deletePictureById(pictureId);
        return ResultUtils.success();
    }


    /**
     * 编辑图片 (用户使用)
     *
     * @param pictureEditRequest 编辑请求
     * @return 返回结果
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest) {
        ThrowUtils.throwIf(pictureEditRequest == null || ObjUtil.isEmpty(pictureEditRequest.getId()) || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isEmpty(pictureEditRequest.getPicName()), ErrorCode.PARAMS_ERROR, "图片名称不能为空");
        ThrowUtils.throwIf(pictureEditRequest.getPicName().length() > 100, ErrorCode.PARAMS_ERROR, "图片名称过长");

        if (StrUtil.isNotEmpty(pictureEditRequest.getPicDesc())) {
            ThrowUtils.throwIf(pictureEditRequest.getPicDesc().length() > 500, ErrorCode.PARAMS_ERROR, "图片介绍过长");
        }
        pictureService.editPicture(pictureEditRequest);
        return ResultUtils.success();
    }


    /**
     * 根据id获取图片详情
     *
     * @param pictureId 图片id
     * @return 图片详情
     */
    @GetMapping("/detail")
    public BaseResponse<PictureDetailVO> getPictureDetailById(@RequestParam Long pictureId) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.getPictureDetailById(pictureId));
    }


    /**
     * 获取用户当前收藏图片
     *
     * @param pictureQueryRequest 查询参数
     * @return 收藏图片分页列表
     */
    @SaUserCheckLogin
    @PostMapping("/collect")
    public BaseResponse<PageVO<PictureHomeVO>> getCollectPictureList(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.getCollectPictureList(pictureQueryRequest));
    }

    /**
     * 获取当前用户 收藏数量 和上传数量
     *
     * @return 用户收藏数量和上传数量
     */
    @SaUserCheckLogin
    @GetMapping("/collect-upload/count")
    public BaseResponse<UserPictureStatsVO> getUserPictureStats() {
        return ResultUtils.success(pictureService.getUserPictureStats());
    }

    /**
     * 获取个人 空间图片分页列表
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 个人空间图片分页列表
     */
    @PostMapping("/personSpace/page")
    public BaseResponse<PageVO<PictureVO>> getPicturePageListAsPersonSpace(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.getPicturePageListAsPersonSpace(pictureQueryRequest));
    }

    /**
     * 获取团队空间图片分页列表
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 团队空间图片分页列表
     */
    @SaUserCheckLogin
    @PostMapping("/teamSpace/page")
    public BaseResponse<PageVO<PictureVO>> getPicturePageListAsTeamSpace(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.getPicturePageListAsTeamSpace(pictureQueryRequest));
    }

    /**
     * 图片下载(仅登录)
     *
     * @param pictureInteractionRequest 图片互动请求
     * @return 原图地址
     */
    @SaUserCheckLogin
    @PostMapping("/download")
    public BaseResponse<String> pictureDownload(@RequestBody PictureInteractionRequest pictureInteractionRequest) {
        ThrowUtils.throwIf(pictureInteractionRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = pictureInteractionRequest.getId();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureId), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.pictureDownload(pictureId));
    }

    /**
     * 颜色搜索(目前仅支持自己的空间)
     *
     * @param searchPictureByColorRequest 搜索请求
     * @return
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<PageVO<PictureVO>> searchPictureByPicColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(searchPictureByColorRequest.getPicColor()), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.isNull(searchPictureByColorRequest.getSpaceId()) || searchPictureByColorRequest.getSpaceId() < 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.searchPictureByPicColor(searchPictureByColorRequest));
    }

    /**
     * 根据图片搜索图片
     *
     * @param searchPictureByPictureRequest 搜索请求
     * @return 搜索结果
     */
    @SaUserCheckLogin
    @PostMapping("/search/by-picture")
    public BaseResponse<List<SearchPictureResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(ObjUtil.isNull(pictureId) || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.searchPictureByPicture(searchPictureByPictureRequest));
    }

    /**
     * 用户点赞或收藏图片
     *
     * @param pictureInteractionRequest 点赞或收藏请求
     * @return 点赞或收藏结果
     */
    @SaUserCheckLogin
    @PostMapping("/interaction")
    public BaseResponse<Boolean> likeOrCollection(@RequestBody PictureInteractionRequest pictureInteractionRequest) {
        ThrowUtils.throwIf(pictureInteractionRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureInteractionRequest.getId() == null || pictureInteractionRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.hasNull(
                        pictureInteractionRequest.getInteractionStatus(),
                        pictureInteractionRequest.getInteractionType()),
                ErrorCode.PARAMS_ERROR);
        //根据枚举 校验类型规范
        if (!PictureInteractionTypeEnum.getKeys().contains(pictureInteractionRequest.getInteractionType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片互动类型错误");
        }
        if (!PictureInteractionStatusEnum.getKeys().contains(pictureInteractionRequest.getInteractionStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片互动状态错误");
        }
        pictureService.likeOrCollection(pictureInteractionRequest);
        return ResultUtils.success();
    }


    /**
     * 创建 AI 扩图任务(普通用户 一天一次 已有图片)
     *
     * @param pictureExtendRequest 扩图任务请求
     * @return 扩图任务信息
     */
    @SaUserCheckLogin
    @PostMapping("/extend")
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureExtendTask(@RequestBody PictureExtendRequest pictureExtendRequest) {
        ThrowUtils.throwIf(pictureExtendRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureExtendRequest.getPictureId() < 0 || ObjUtil.isNull(pictureExtendRequest.getPictureId()), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.createPictureExtendTask(pictureExtendRequest));
    }

    /**
     * 获取 AI 扩图任务
     *
     * @param taskId 任务id
     * @return 扩图结果
     */
    @GetMapping("/extend/query")
    public BaseResponse<GetOutPaintingTaskResponse> queryPictureExtendTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.queryPictureExtendTask(taskId));
    }


    /**
     * 爬取图片(此时还没有上传)
     *
     * @param capturePictureRequest 爬取请求
     * @return 爬取的结果
     */
    @PostMapping("/capture")
    public BaseResponse<List<CapturePictureResult>> capturePicture(@RequestBody capturePictureRequest capturePictureRequest) {
        ThrowUtils.throwIf(capturePictureRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(capturePictureRequest.getSearchText()), ErrorCode.PARAMS_ERROR, "搜索词不能为空");
        ThrowUtils.throwIf(capturePictureRequest.getCaptureCount() > 20, ErrorCode.PARAMS_ERROR, "一次最多爬取20张图片");
        return ResultUtils.success(pictureService.capturePicture(capturePictureRequest));
    }

    /**
     * 上传爬取图片
     *
     * @param pictureUploadRequest 上传请求
     * @return 上传结果
     */
    @SaUserCheckLogin
    @PostMapping("/upload/capture")
    public BaseResponse<Boolean> uploadPictureByCapture(@RequestBody PictureUploadRequest pictureUploadRequest) {
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(pictureUploadRequest.getPictureUrl()), ErrorCode.PARAMS_ERROR, "图片地址不能为空");
        pictureService.uploadPictureByCapture(pictureUploadRequest);
        return ResultUtils.success();
    }

    //    ----------------管理使用--------------

    /**
     * 审核图片(包含批量处理)
     *
     * @param pictureReviewRequest 审核请求
     * @return 审核结果
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewPicture(@RequestBody PictureReviewRequest pictureReviewRequest) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureReviewRequest.getId() == null && CollUtil.isEmpty(pictureReviewRequest.getIdList()), ErrorCode.PARAMS_ERROR);
        pictureService.reviewPicture(pictureReviewRequest);
        return ResultUtils.success();
    }


    /**
     * 管理员 批量更新
     *
     * @param pictureUpdateRequest 更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureService.updatePicture(pictureUpdateRequest);
        return ResultUtils.success();
    }


    /**
     * 分页获取图片列表 (仅管理员可用)
     *
     * @param pictureQueryRequest 查询请求
     * @return 图片列表
     */
    @PostMapping("/manage/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PageVO<PictureVO>> getPicturePageListAsManage(@RequestBody PictureQueryRequest
                                                                              pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(pictureService.getPicturePageListAsManage(pictureQueryRequest));
    }

}

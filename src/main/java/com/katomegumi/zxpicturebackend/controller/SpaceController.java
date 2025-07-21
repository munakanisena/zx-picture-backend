package com.katomegumi.zxpicturebackend.controller;


import cn.hutool.core.util.ObjectUtil;
import com.katomegumi.zxpicturebackend.core.annotation.AuthCheck;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.req.DeleteRequest;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import com.katomegumi.zxpicturebackend.core.constant.ApiRouterConstant;
import com.katomegumi.zxpicturebackend.core.constant.UserConstant;
import com.katomegumi.zxpicturebackend.manager.auth.annotation.SaUserCheckLogin;
import com.katomegumi.zxpicturebackend.model.dto.space.*;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceTeamDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceVO;
import com.katomegumi.zxpicturebackend.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author Megumi
 */
@SaUserCheckLogin
@RestController
@Slf4j
@RequestMapping(ApiRouterConstant.API_SPACE_URL_PREFIX)
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;


    /**
     * 激活空间
     *
     * @param spaceActiveRequest 激活请求
     * @return 激活结果
     */
    @PostMapping("/active")
    public BaseResponse<Boolean> activeSpace(@RequestBody SpaceActiveRequest spaceActiveRequest) {
        ThrowUtils.throwIf(spaceActiveRequest == null, ErrorCode.PARAMS_ERROR);
        spaceService.activeSpace(spaceActiveRequest);
        return ResultUtils.success();
    }

    /**
     * 编辑空间(用户)
     *
     * @param spaceEditRequest 空间编辑请求
     * @return 是否成功
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest) {
        ThrowUtils.throwIf(spaceEditRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceEditRequest.getId() == null || spaceEditRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        spaceService.editSpace(spaceEditRequest);
        return ResultUtils.success();
    }

    /**
     * 删除空间
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = deleteRequest.getId();
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceId) || spaceId < 0, ErrorCode.PARAMS_ERROR);
        spaceService.deleteSpace(spaceId);
        return ResultUtils.success();
    }

    /**
     * 登录用户获取私人空间详情
     *
     * @return 私人空间详情
     */
    @GetMapping("/private/space-detail")
    public BaseResponse<SpaceDetailVO> getPrivateSpaceDetailByLoginUser() {
        return ResultUtils.success(spaceService.getSpaceDetailByLoginUser());
    }

    /**
     * 登录用户获取团队空间详情
     *
     * @return 团队空间详情
     */
    @GetMapping("/team/space-detail")
    public BaseResponse<SpaceTeamDetailVO> getTeamSpaceDetailByLoginUser() {
        return ResultUtils.success(spaceService.getTeamSpaceDetailByLoginUser());
    }

    /**
     * 获取登录用户加入的团队空间列表
     *
     * @return 空间详情
     */
    @GetMapping("/join/team-spaces")
    public BaseResponse<List<SpaceTeamDetailVO>> getJoinTeamSpacesByLoginUser() {
        return ResultUtils.success(spaceService.getJoinTeamSpacesByLoginUser());
    }

    /**
     * 根据空间ID获取空间详情
     *
     * @return 空间详情
     */
    @GetMapping("/detail")
    public BaseResponse<SpaceDetailVO> getSpaceDetailBySpaceId(Long spaceId) {
        ThrowUtils.throwIf(spaceId == null || spaceId < 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceService.getSpaceDetailBySpaceId(spaceId));
    }

    /**
     * 切换当前操作的空间上下文
     *
     * @param spaceSwitchRequest 空间切换请求
     * @return 切换结果
     */
    @PostMapping("/switch")
    public BaseResponse<Boolean> switchSpaceContext(@RequestBody SpaceSwitchRequest spaceSwitchRequest) {
        ThrowUtils.throwIf(spaceSwitchRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceSwitchRequest.getSpaceId();
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceId) || spaceId < 0, ErrorCode.PARAMS_ERROR);
        spaceService.switchSpaceContext(spaceId);
        return ResultUtils.success();
    }


    //---------------仅管理员使用---------------

    /**
     * 更新空间
     *
     * @param spaceUpdateRequest 更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        ThrowUtils.throwIf(spaceUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceUpdateRequest.getId() == null || spaceUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        spaceService.updateSpace(spaceUpdateRequest);
        return ResultUtils.success();
    }

    /**
     * 分页获取空间列表
     *
     * @param spaceQueryRequest 空间查询请求
     * @return 空间列表
     */
    @PostMapping("/manage/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PageVO<SpaceVO>> getSpacePageListAsManage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceService.getSpacePageListAsManage(spaceQueryRequest));
    }


}

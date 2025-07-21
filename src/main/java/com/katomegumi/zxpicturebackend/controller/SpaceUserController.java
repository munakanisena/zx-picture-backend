package com.katomegumi.zxpicturebackend.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.katomegumi.zxpicturebackend.core.annotation.AuthCheck;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.req.DeleteRequest;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import com.katomegumi.zxpicturebackend.core.constant.ApiRouterConstant;
import com.katomegumi.zxpicturebackend.core.constant.UserConstant;
import com.katomegumi.zxpicturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.katomegumi.zxpicturebackend.manager.auth.annotation.SaUserCheckLogin;
import com.katomegumi.zxpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.katomegumi.zxpicturebackend.model.enums.SpaceRoleEnum;
import com.katomegumi.zxpicturebackend.model.vo.space.user.SpaceUserVO;
import com.katomegumi.zxpicturebackend.service.SpaceUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Megumi
 * @description 空间用户管理
 */
@SaUserCheckLogin
@RestController
@RequestMapping(ApiRouterConstant.API_SPACE_USER_URL_PREFIX)
@Slf4j
@RequiredArgsConstructor
public class SpaceUserController {


    private final SpaceUserService spaceUserService;

    /**
     * 添加空间用户成员
     *
     * @param spaceUserAddRequest 成员添加请求
     * @return 添加结果
     */
    @PostMapping("/add-member")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.hasNull(spaceUserAddRequest.getSpaceId(), spaceUserAddRequest.getUserId()), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(spaceUserAddRequest.getSpaceRole()), ErrorCode.PARAMS_ERROR);
        //根据枚举校验
        ThrowUtils.throwIf(!SpaceRoleEnum.getKeys().contains(spaceUserAddRequest.getSpaceRole()), ErrorCode.PARAMS_ERROR);
        spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success();
    }

    /**
     * 移除空间成员
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceUserId = deleteRequest.getId();
        ThrowUtils.throwIf(spaceUserId == null || spaceUserId < 0, ErrorCode.PARAMS_ERROR);
        spaceUserService.deleteSpaceUser(spaceUserId);
        return ResultUtils.success();
    }

    /**
     * 编辑用户成员
     *
     * @param spaceUserEditRequest 编辑请求
     * @return 编辑结果
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        ThrowUtils.throwIf(spaceUserEditRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceUserEditRequest.getId() == null || spaceUserEditRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!SpaceRoleEnum.getKeys().contains(spaceUserEditRequest.getSpaceRole()), ErrorCode.PARAMS_ERROR);
        spaceUserService.editSpaceUser(spaceUserEditRequest);
        return ResultUtils.success();
    }

    /**
     * 根据空间ID获取团队空间成员列表
     *
     * @param spaceId 团队空间ID
     * @return 团队空间成员列表
     */
    @GetMapping("/team/members")
    public BaseResponse<List<SpaceUserVO>> getTeamSpaceMembersBySpaceId(Long spaceId) {
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
        List<SpaceUserVO> members = spaceUserService.getTeamSpaceMembersBySpaceId(spaceId);
        return ResultUtils.success(members);
    }

    //-----------------管理员使用----------------------

    /**
     * 管理员查询某个用户在各个加入的团队空间的权限
     *
     * @param spaceUserQueryRequest 查询请求
     * @return 用户空间信息
     */
    @PostMapping("/get/space-permission")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<SpaceUserVO>> queryUserTeamSpacePermissions(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(spaceUserService.queryUserTeamSpacePermissions(spaceUserQueryRequest));
    }
}

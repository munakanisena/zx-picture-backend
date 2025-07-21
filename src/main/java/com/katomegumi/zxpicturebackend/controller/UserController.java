package com.katomegumi.zxpicturebackend.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.katomegumi.zxpicturebackend.core.annotation.AuthCheck;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.req.DeleteRequest;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import com.katomegumi.zxpicturebackend.core.constant.ApiRouterConstant;
import com.katomegumi.zxpicturebackend.core.constant.UserConstant;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.manager.auth.annotation.SaUserCheckLogin;
import com.katomegumi.zxpicturebackend.manager.email.model.EmailRequest;
import com.katomegumi.zxpicturebackend.model.dto.user.*;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.user.UserVO;
import com.katomegumi.zxpicturebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author Megumi
 */
@RestController
@RequestMapping(ApiRouterConstant.API_USER_URL_PREFIX)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 发送邮箱注册验证码
     *
     * @param emailRequest 邮件请求
     * @return 发送结果
     */
    @PostMapping("/captcha")
    public BaseResponse<Boolean> sendRegisterCaptcha(@RequestBody EmailRequest emailRequest) {
        ThrowUtils.throwIf(emailRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(emailRequest.getUserEmail()), ErrorCode.PARAMS_ERROR, "参数为空");
        userService.sendEmailRegisterCaptcha(emailRequest);
        return ResultUtils.success();
    }


    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Boolean> register(@RequestBody UserRegisterRequest userRegisterRequest) {

        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);

        String username = userRegisterRequest.getName();
        String userEmail = userRegisterRequest.getEmail();
        String captcha = userRegisterRequest.getCaptcha();
        String userPassword = userRegisterRequest.getPassword();
        String confirmPassword = userRegisterRequest.getConfirmPassword();

        if (StrUtil.hasBlank(username, userPassword, confirmPassword, userEmail, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        userService.register(userRegisterRequest);

        return ResultUtils.success();
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @return 用户详情
     */
    @PostMapping("/login")
    public BaseResponse<UserDetailVO> login(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);

        String emailOrUsername = userLoginRequest.getEmailOrUsername();
        String password = userLoginRequest.getPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(emailOrUsername, password), ErrorCode.PARAMS_ERROR, "参数为空");

        UserDetailVO loginUserDetailVO = userService.login(userLoginRequest);
        return ResultUtils.success(loginUserDetailVO);
    }

    /**
     * 密码重置 1
     *
     * @param emailRequest 邮箱地址
     * @return
     */
    @PostMapping("/forgot/one")
    public BaseResponse<Boolean> forgotPassword(@RequestBody EmailRequest emailRequest) {
        ThrowUtils.throwIf(emailRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(emailRequest.getUserEmail()), ErrorCode.PARAMS_ERROR, "参数为空");
        userService.forgotPassword(emailRequest);
        return ResultUtils.success();
    }

    /**
     * 密码重置 2
     *
     * @param userResetPasswordRequest 密码重置请求
     * @return
     */
    @PostMapping("/forgot/two")
    public BaseResponse<Boolean> restPassword(@RequestBody UserResetPasswordRequest userResetPasswordRequest) {
        ThrowUtils.throwIf(userResetPasswordRequest == null, ErrorCode.PARAMS_ERROR);
        String userEmail = userResetPasswordRequest.getEmail();
        String captcha = userResetPasswordRequest.getCaptcha();
        String newPassword = userResetPasswordRequest.getNewPassword();
        String confirmPassword = userResetPasswordRequest.getConfirmPassword();

        if (StrUtil.hasBlank(userEmail, captcha, newPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        userService.resetPassword(userResetPasswordRequest);
        return ResultUtils.success();
    }

    /**
     * 用户退出 清除登录态
     *
     * @return 是否成功
     */
    @SaUserCheckLogin
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout() {
        userService.userLogout();
        return ResultUtils.success();
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户信息
     */
    @SaUserCheckLogin
    @GetMapping("/loginDetail")
    public BaseResponse<UserDetailVO> getLoginUserDetail() {
        if (!StpKit.USER.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(userService.getLoginUserDetail());
    }


    /**
     * 用户编辑自身信息
     *
     * @param userEditRequest 用户信息
     * @return 是否成功
     */
    @SaUserCheckLogin
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserInfo(@RequestBody UserEditRequest userEditRequest) {
        if (!StpKit.USER.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        ThrowUtils.throwIf(userEditRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userEditRequest.getId() == null || userEditRequest.getId() < 0, ErrorCode.PARAMS_ERROR, "用户id错误");
        String userIntroduction = userEditRequest.getIntroduction();
        if (StrUtil.isNotEmpty(userIntroduction) && userIntroduction.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户简介过长");
        }
        userService.editUserInfo(userEditRequest);
        return ResultUtils.success();
    }

    /**
     * 上传头像
     *
     * @param avatarFile 头像文件
     * @return 头像地址
     */
    @SaUserCheckLogin
    @PostMapping("/upload-avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile avatarFile) {
        ThrowUtils.throwIf(avatarFile == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.uploadAvatar(avatarFile));
    }


    /**
     * 根据用户ID获取用户信息详情
     *
     * @param userId 用户ID
     * @return 用户信息详情
     */
    @GetMapping("/detail")
    public BaseResponse<UserDetailVO> getUserDetailById(Long userId) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userId), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.getUserDetailById(userId));
    }

    //-------------管理员使用------------


    /**
     * 删除用户
     *
     * @param deleteRequest 用户ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long userId = deleteRequest.getId();
        ThrowUtils.throwIf(userId == null || userId < 0, ErrorCode.PARAMS_ERROR);
        userService.deleteUserById(userId);
        return ResultUtils.success();
    }

    /**
     * 管理员更新用户信息
     *
     * @param userUpdateRequest 用户信息
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userUpdateRequest.getId()) || userUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        userService.updateUserById(userUpdateRequest);
        return ResultUtils.success();
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/manage/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PageVO<UserVO>> getUserPageListAsManage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.getUserPageListAsManage(userQueryRequest));
    }


}

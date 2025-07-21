package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.manager.email.model.EmailRequest;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.dto.user.*;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.user.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lirui
 * @description 针对表【user_info(用户信息表)】的数据库操作Service
 * @createDate 2025-05-07 17:17:08
 */
public interface UserService extends IService<UserInfo> {
    /**
     * 发送邮件验证码
     *
     * @param emailRequest 用户邮箱
     */
    void sendEmailRegisterCaptcha(EmailRequest emailRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 用户注册信息
     */
    void register(UserRegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录信息
     * @return 登录用户信息
     */
    UserDetailVO login(UserLoginRequest userLoginRequest);

    /**
     * 发送密码重置邮件
     *
     * @param emailRequest 用户邮箱
     */
    void forgotPassword(EmailRequest emailRequest);


    /**
     * 重置密码
     *
     * @param userResetPasswordRequest 用户重置密码信息
     */
    void resetPassword(UserResetPasswordRequest userResetPasswordRequest);

    /**
     * 用户退出 清除sa-token登录态
     */
    void userLogout();

    /**
     * 获取用户详情
     *
     * @return 用户详情
     */
    UserDetailVO getLoginUserDetail();

    /**
     * 上传用户头像
     *
     * @param avatarFile 用户头像文件
     * @return 用户头像url
     */
    String uploadAvatar(MultipartFile avatarFile);

    /**
     * 用户 自身编辑
     *
     * @param userEditRequest 用户编辑信息
     */
    void editUserInfo(UserEditRequest userEditRequest);

    /**
     * 通过id获取用户
     *
     * @param userId 用户id
     * @return 用户详情
     */
    UserDetailVO getUserDetailById(Long userId);


    /**
     * 删除用户 管理员权限
     *
     * @param id 用户id
     */
    void deleteUserById(Long id);

    /**
     * 管理员更新用户信息
     *
     * @param userUpdateRequest 用户更新信息
     */
    void updateUserById(UserUpdateRequest userUpdateRequest);

    /**
     * 分页查询用户信息 管理员权限
     *
     * @param userQueryRequest 用户查询信息
     * @return 用户分页列表
     */
    PageVO<UserVO> getUserPageListAsManage(UserQueryRequest userQueryRequest);

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    UserInfo getCurrentUserInfo();


    /**
     * 校验用户是否是管理员
     *
     * @param userInfo 用户信息
     * @return true 是管理员
     */
    Boolean isAdmin(UserInfo userInfo);

}

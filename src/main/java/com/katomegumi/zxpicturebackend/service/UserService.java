package com.katomegumi.zxpicturebackend.service;

import com.katomegumi.zxpicturebackend.manager.email.model.EmailRequest;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.model.dto.user.*;
import com.katomegumi.zxpicturebackend.model.vo.PageVO;
import com.katomegumi.zxpicturebackend.model.vo.user.LoginUserDetailVO;
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
     * @param emailRequest 用户邮箱
     * @return 发送成功
     */
    void sendEmailRegisterCaptcha(EmailRequest emailRequest);

    /**
     * 用户注册
     * @param registerRequest 用户注册信息
     * @return
     */
    void register(UserRegisterRequest registerRequest);

    /**
     * 用户登录
     * @param userLoginRequest 用户登录信息
     * @return
     */
    LoginUserDetailVO login(UserLoginRequest userLoginRequest);

    /**
     * 发送密码重置邮件
     * @param emailRequest
     */
    void forgotPassword(EmailRequest emailRequest);


    /**
     * 重置密码
     * @param userResetPasswordRequest
     */
    void resetPassword(UserResetPasswordRequest userResetPasswordRequest);

    /**
     * 用户退出 清除sa-token登录态
     */
    void userLogout();

    /**
     * 获取用户详情
     * @return 用户详情
     */
    LoginUserDetailVO getLoginUserDetail();

    /**
     * 上传用户头像
     * @param avatarFile
     * @return 用户头像url
     */
    String uploadAvatar(MultipartFile avatarFile);

    /**
     * 用户 自身编辑
     * @param userEditRequest
     */
    void editUserInfo(UserEditRequest userEditRequest);

    /**
     * 通过id获取用户
     * @param userId
     * @return
     */
    UserDetailVO getUserDetailById(Long userId);


    /**
     * 删除用户 管理员权限
     * @param id 用户id
     */
    void deleteUserById(Long id);

    /**
     * 管理员更新用户信息
     * @param userUpdateRequest
     */
    void updateUserById(UserUpdateRequest userUpdateRequest);

    /**
     * 分页查询用户信息 管理员权限
     * @param userQueryRequest
     * @return
     */
    PageVO<UserVO> getUserPageListAsManage(UserQueryRequest userQueryRequest);
}

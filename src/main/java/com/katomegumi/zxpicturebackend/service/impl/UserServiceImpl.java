package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.constant.CacheConstant;
import com.katomegumi.zxpicturebackend.core.constant.UserConstant;
import com.katomegumi.zxpicturebackend.core.util.SFunctionUtils;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.manager.cache.VerifyCaptchaCacheManager;
import com.katomegumi.zxpicturebackend.manager.email.EmailManager;
import com.katomegumi.zxpicturebackend.manager.email.model.EmailRequest;
import com.katomegumi.zxpicturebackend.manager.upload.PictureFileUpload;
import com.katomegumi.zxpicturebackend.manager.upload.modal.UploadPictureResult;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.dao.mapper.UserInfoMapper;
import com.katomegumi.zxpicturebackend.model.dto.user.*;
import com.katomegumi.zxpicturebackend.model.enums.UserDisabledEnum;
import com.katomegumi.zxpicturebackend.model.enums.UserRoleEnum;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.user.UserVO;
import com.katomegumi.zxpicturebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.katomegumi.zxpicturebackend.core.constant.CacheConstant.USER.USER_LOGIN_STATE;


/**
 * @author Megumi
 * @description 针对表【user_info(用户信息表)】的数据库操作Service实现
 * @createDate 2025-05-07 17:17:08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
        implements UserService {

    private final UserInfoMapper userInfoMapper;

    private final EmailManager emailManager;

    private final VerifyCaptchaCacheManager verifyCaptchaCacheManager;

    private final PictureFileUpload pictureFileUpload;


    @Override
    public void sendEmailRegisterCaptcha(EmailRequest emailRequest) {
        String userEmail = emailRequest.getUserEmail();
        //1.邮箱格式是否正确
        ThrowUtils.throwIf(!ReUtil.isMatch(RegexPool.EMAIL, userEmail), ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        //2.判断邮箱是否已经注册
        Long count = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, userEmail));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱已被注册");
        }
        //3.发送验证码
        emailManager.sendEmailCaptcha(userEmail);
    }

    @Override
    public void register(UserRegisterRequest registerRequest) {

        String username = registerRequest.getName();
        String userEmail = registerRequest.getEmail();
        String captcha = registerRequest.getCaptcha();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();

        //1.校验邮箱是否合法
        ThrowUtils.throwIf(!ReUtil.isMatch(RegexPool.EMAIL, userEmail), ErrorCode.PARAMS_ERROR, "邮箱格式错误");

        //2.用户名是否可用
        Long count = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getName, password));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已被存在,请重新输入");
        }

        //3.校验密码长度 用户名称长度
        ThrowUtils.throwIf(password.length() < 8 || confirmPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        ThrowUtils.throwIf(username.length() > 16, ErrorCode.PARAMS_ERROR, "用户账号长度大于16位");

        //4.校验
        ThrowUtils.throwIf(!password.equals(confirmPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        //5.校验验证码
        String key = CacheConstant.EMAIL.REGISTER + userEmail;
        if (!verifyCaptchaCacheManager.verifyCaptchaOk(key, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        //6.构造用户
        UserInfo userInfo = new UserInfo();
        userInfo.setName(username);
        userInfo.setPassword(DigestUtils.md5DigestAsHex((password + UserConstant.SALT).getBytes()));
        userInfo.setEmail(userEmail);
        boolean result = this.save(userInfo);

        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //7.删除验证码
        verifyCaptchaCacheManager.removeCaptcha(key);
    }


    @Override
    public UserDetailVO login(UserLoginRequest userLoginRequest) {
        String emailOrUsername = userLoginRequest.getEmailOrUsername();
        String password = userLoginRequest.getPassword();

        ThrowUtils.throwIf(password.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度不能小于8");
        String encryptPassword = DigestUtils.md5DigestAsHex((password + UserConstant.SALT).getBytes());

        UserInfo userInfo;
        if (ReUtil.isMatch(RegexPool.EMAIL, emailOrUsername)) {
            //用邮箱去查询
            userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                    .eq(UserInfo::getEmail, emailOrUsername)
                    .eq(UserInfo::getPassword, encryptPassword));
        } else {
            //反之用用户名去查询
            userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                    .eq(UserInfo::getName, emailOrUsername)
                    .eq(UserInfo::getPassword, encryptPassword));
        }
        // 查询都用户不存在
        if (userInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        //用户是否被禁用
        ThrowUtils.throwIf(UserDisabledEnum.isDisabled(userInfo.getIsDisabled()), ErrorCode.FORBIDDEN_ERROR, "用户已被禁用");

        //默认为cookie记住模式
        StpKit.USER.login(userInfo.getId());
        StpKit.USER.getSession().set(USER_LOGIN_STATE, userInfo);

        return BeanUtil.toBean(userInfo, UserDetailVO.class);
    }

    @Override
    public void forgotPassword(EmailRequest emailRequest) {
        //1.校验邮箱格式
        String userEmail = emailRequest.getUserEmail();
        ThrowUtils.throwIf(!ReUtil.isMatch(RegexPool.EMAIL, userEmail), ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        //2.查询用户是否存在
        Long count = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, userEmail));
        if (count < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        //3.发送邮件
        emailManager.sendEmailForgotPassword(userEmail);
    }


    @Override
    public void resetPassword(UserResetPasswordRequest userResetPasswordRequest) {
        String userEmail = userResetPasswordRequest.getEmail();
        String captcha = userResetPasswordRequest.getCaptcha();
        String newPassword = userResetPasswordRequest.getNewPassword();
        String confirmPassword = userResetPasswordRequest.getConfirmPassword();

        //1.校验邮箱是否合法
        ThrowUtils.throwIf(!ReUtil.isMatch(RegexPool.EMAIL, userEmail), ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        //3.校验密码长度
        ThrowUtils.throwIf(newPassword.length() < 8 || confirmPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        //4.校验
        ThrowUtils.throwIf(!newPassword.equals(confirmPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        //5.校验验证码
        String key = CacheConstant.EMAIL.FORGOT + userEmail;
        if (!verifyCaptchaCacheManager.verifyForgotCaptchaOk(key, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        //6.更新密码
        String encryptPassword = DigestUtils.md5DigestAsHex((newPassword + UserConstant.SALT).getBytes());
        boolean result = this.update(new LambdaUpdateWrapper<UserInfo>().eq(UserInfo::getEmail, userEmail).set(UserInfo::getPassword, encryptPassword));
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //7.删除验证码
        verifyCaptchaCacheManager.removeCaptcha(key);
    }

    @Override
    public void userLogout() {
        StpKit.USER.logout();
        StpKit.SPACE.logout();
    }

    @Override
    public UserDetailVO getLoginUserDetail() {
        UserInfo userInfo = this.getCurrentUserInfo();
        return BeanUtil.copyProperties(userInfo, UserDetailVO.class);
    }


    @Override
    public String uploadAvatar(MultipartFile avatarFile) {
        long userId = StpKit.USER.getLoginIdAsLong();
        //1. 上传地址前缀 avatar/用户id/图片名称
        String pathPrefix = "avatar/" + userId + "/";
        //2.上头图片
        UploadPictureResult uploadPictureResult = pictureFileUpload.uploadPicture(avatarFile, pathPrefix, false);
        //3.设置用户图片
        String avatarUrl = uploadPictureResult.getOriginUrl();
        ThrowUtils.throwIf(StrUtil.isBlank(avatarUrl), ErrorCode.OPERATION_ERROR, "上传头像失败");
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setAvatar(avatarUrl);
        boolean result = this.updateById(userInfo);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "头像更新失败");
        //4.删除缓存
        StpKit.USER.getSession().clear();
        //5.返回图片地址
        return avatarUrl;
    }

    @Override
    public void editUserInfo(UserEditRequest userEditRequest) {
        UserInfo userInfo = this.getById(userEditRequest.getId());
        ThrowUtils.throwIf(userInfo == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        //是否为本人操作
        Long loginUserId = StpKit.USER.getLoginIdAsLong();
        ThrowUtils.throwIf(
                !loginUserId.equals(userEditRequest.getId()),
                ErrorCode.NO_AUTH_ERROR,
                "无权限修改他人信息"
        );
        userInfo = BeanUtil.copyProperties(userEditRequest, UserInfo.class);
        boolean result = this.updateById(userInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息修改失败");
        }
        //清除旧缓存
        StpKit.USER.getSession().clear();
    }

    @Override
    public UserDetailVO getUserDetailById(Long userId) {
        //未登录就创建对应session
        UserInfo userInfo = (UserInfo) StpKit.USER.getSessionByLoginId(userId, true).get(USER_LOGIN_STATE);
        if (userInfo == null) {
            //2.查看数据库中是否有该用户
            userInfo = this.getById(userId);
            StpKit.USER.getSessionByLoginId(userId).set(USER_LOGIN_STATE, userInfo);
            ThrowUtils.throwIf(userInfo == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return BeanUtil.copyProperties(userInfo, UserDetailVO.class);
    }

    @Override
    public void deleteUserById(Long id) {
        UserInfo userInfo = this.getById(id);
        if (userInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息删除失败");
        }
        //清除登录态
        StpKit.USER.logout(userInfo.getId());
    }


    @Override
    public void updateUserById(UserUpdateRequest userUpdateRequest) {
        UserInfo userInfo = this.getById(userUpdateRequest.getId());
        ThrowUtils.throwIf(userInfo == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        userInfo = BeanUtil.copyProperties(userUpdateRequest, UserInfo.class);
        boolean result = this.updateById(userInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息修改失败");
        }
        StpKit.USER.getSessionByLoginId(userUpdateRequest.getId()).clear();
    }

    @Override
    public PageVO<UserVO> getUserPageListAsManage(UserQueryRequest userQueryRequest) {
        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper = getLambdaQueryWrapper(userQueryRequest);
        Page<UserInfo> page = this.page(userQueryRequest.getPage(UserInfo.class), lambdaQueryWrapper);
        return new PageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                Optional.
                        ofNullable(page.getRecords()).
                        orElse(Collections.emptyList())
                        .stream()
                        .map(userInfo -> BeanUtil.copyProperties(userInfo, UserVO.class))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public UserInfo getCurrentUserInfo() {
        UserInfo userInfo = (UserInfo) StpKit.USER.getSession().get(USER_LOGIN_STATE);
        if (userInfo == null) {
            userInfo = this.getById(StpKit.USER.getLoginIdAsLong());
            //重新设置
            StpKit.USER.getSession().set(USER_LOGIN_STATE, userInfo);
        }
        return userInfo;
    }

    @Override
    public Boolean isAdmin(UserInfo userInfo) {
        return UserRoleEnum.isAdmin(userInfo.getRole());
    }


    /**
     * 构造查询参数
     *
     * @param userQueryRequest 查询请求
     * @return 查询参数
     */
    private LambdaQueryWrapper<UserInfo> getLambdaQueryWrapper(UserQueryRequest userQueryRequest) {
        Long userId = userQueryRequest.getId();
        String username = userQueryRequest.getName();
        String userEmail = userQueryRequest.getEmail();
        String userPhone = userQueryRequest.getPhone();
        Long vipNumber = userQueryRequest.getVipNumber();
        String userRole = userQueryRequest.getRole();
        Integer isVip = userQueryRequest.getIsVip();
        Integer isDisabled = userQueryRequest.getIsDisabled();

        String sortField = userQueryRequest.getSortField();
        Boolean sortOrder = userQueryRequest.getSortOrder();

        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .like(ObjUtil.isNotNull(userId), UserInfo::getId, userId)
                .like(StrUtil.isNotBlank(username), UserInfo::getName, username)
                .like(StrUtil.isNotBlank(userEmail), UserInfo::getEmail, userEmail)
                .like(StrUtil.isNotBlank(userPhone), UserInfo::getPhone, userPhone)
                .eq(ObjUtil.isNotNull(vipNumber), UserInfo::getVipNumber, vipNumber)
                .eq(StrUtil.isNotBlank(userRole), UserInfo::getRole, userRole)
                .eq(ObjUtil.isNotNull(isVip), UserInfo::getIsVip, isVip)
                .eq(ObjUtil.isNotNull(isDisabled), UserInfo::getIsDisabled, isDisabled);
        //构造排序
        if (sortField != null) {
            lambdaQueryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder, SFunctionUtils.getSFunction(UserInfo.class, sortField));
        } else {
            lambdaQueryWrapper.orderByDesc(UserInfo::getCreateTime);
        }
        return lambdaQueryWrapper;
    }


}







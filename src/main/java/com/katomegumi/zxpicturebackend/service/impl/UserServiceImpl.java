package com.katomegumi.zxpicturebackend.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.constant.RedisConstant;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.manager.email.EmailManager;
import com.katomegumi.zxpicturebackend.manager.email.model.EmailRequest;
import com.katomegumi.zxpicturebackend.manager.redis.VerifyCaptchaManager;
import com.katomegumi.zxpicturebackend.manager.upload.PictureFileUpload;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.manager.upload.modal.UploadPictureResult;
import com.katomegumi.zxpicturebackend.model.dto.user.*;
import com.katomegumi.zxpicturebackend.model.enums.UserDisabledEnum;
import com.katomegumi.zxpicturebackend.model.vo.PageVO;
import com.katomegumi.zxpicturebackend.model.vo.user.LoginUserDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.user.UserVO;
import com.katomegumi.zxpicturebackend.service.UserService;
import com.katomegumi.zxpicturebackend.model.dao.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.katomegumi.zxpicturebackend.core.constant.UserConstant.USER_LOGIN_STATE;


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

    private final VerifyCaptchaManager verifyCaptchaManager;

    private final StringRedisTemplate stringRedisTemplate;

    private final PictureFileUpload pictureFileUpload;

    //加盐值
    private final String SALT="Megumi";

    @Override
    public void sendEmailRegisterCaptcha(EmailRequest emailRequest) {
        String userEmail = emailRequest.getUserEmail();
        //1.邮箱格式是否正确
        ThrowUtils.throwIf(!ReUtil.isMatch(RegexPool.EMAIL,userEmail), ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        //2.判断邮箱是否已经注册
        Long count = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUserEmail, userEmail));
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱已被注册");
        }
        //3.发送验证码
        emailManager.sendEmailCaptcha(userEmail);
    }

    @Override
    public void register(UserRegisterRequest registerRequest) {

        String username = registerRequest.getUsername();
        String userEmail = registerRequest.getUserEmail();
        String captcha = registerRequest.getCaptcha();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();

        //1.校验邮箱是否合法
        ThrowUtils.throwIf(!ReUtil.isMatch(RegexPool.EMAIL,userEmail), ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        //2.校验是否存在 用户
        Long count = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUserEmail, userEmail));
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱已被注册");
        }
        count = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUsername, password));

        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名已被存在,请重新输入");
        }
        //3.校验密码长度 用户名称长度
        ThrowUtils.throwIf(password.length() < 8||confirmPassword.length()<8, ErrorCode.PARAMS_ERROR,"密码长度不能小于8位");
        ThrowUtils.throwIf(username.length()<4||username.length()>16, ErrorCode.PARAMS_ERROR,"用户账号长度不能小于4位,大于16位");

        //4.校验
        ThrowUtils.throwIf(!password.equals(confirmPassword), ErrorCode.PARAMS_ERROR,"两次密码不一致");
        //5.校验验证码
        String key= RedisConstant.EMAIL.REGISTER+userEmail;
        if (!verifyCaptchaManager.verifyCaptchaOk(key,captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        //6.构造用户
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPassword(DigestUtils.md5DigestAsHex((password+SALT).getBytes()));
        userInfo.setUserEmail(userEmail);
        boolean result = this.save(userInfo);

        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //7.删除验证码
        verifyCaptchaManager.removeCaptcha(key);
    }


    @Override
    public LoginUserDetailVO login(UserLoginRequest userLoginRequest) {
        String emailOrUsername = userLoginRequest.getEmailOrUsername();
        String password = userLoginRequest.getPassword();

        ThrowUtils.throwIf(password.length()<8, ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        String encryptPassword = DigestUtils.md5DigestAsHex((password + SALT).getBytes());

        UserInfo userInfo;
        if (ReUtil.isMatch(RegexPool.EMAIL,emailOrUsername)){
            //用邮箱去查询
            userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                    .eq(UserInfo::getUserEmail, emailOrUsername)
                    .eq(UserInfo::getPassword, encryptPassword));
        }else {
            //反之用用户名去查询
            userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                    .eq(UserInfo::getUsername, emailOrUsername)
                    .eq(UserInfo::getPassword, encryptPassword));
        }
        // 查询都用户不存在
        if (userInfo==null) {
            log.error("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        //request.getSession().setAttribute(USER_LOGIN_STATE, userInfo); 全局使用sa-token

        //用户是否被禁用
        ThrowUtils.throwIf(UserDisabledEnum.isDisabled(userInfo.getIsDisabled()), ErrorCode.FORBIDDEN_ERROR, "用户已被禁用");

        //默认为cookie记住模式
        StpKit.USER.login(userInfo.getId());
        //StpKit.USER.getSession().set(USER_LOGIN_STATE, userInfo);
        //这里存入redis 当用户访问其他用户的主页时可以走redis
        SaTokenInfo tokenInfo = StpKit.USER.getTokenInfo();
        stringRedisTemplate.opsForValue()
                .set(USER_LOGIN_STATE+userInfo.getId(), JSONUtil.toJsonStr(userInfo)
                        , tokenInfo.getTokenTimeout(), TimeUnit.SECONDS);

        //采用sa-TOKEN 这里是便于空间用户权限使用 todo 后续可能需要修改
        StpKit.SPACE.login(userInfo.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE, userInfo);

        return BeanUtil.toBean(userInfo, LoginUserDetailVO.class);
    }

    @Override
    public void forgotPassword(EmailRequest emailRequest) {
        //1.校验邮箱格式
        String userEmail = emailRequest.getUserEmail();
        ThrowUtils.throwIf(!ReUtil.isMatch(RegexPool.EMAIL,userEmail), ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        //2.查询用户是否存在
        Long count = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUserEmail, userEmail));
        if (count < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        //3.发送邮件
        emailManager.sendEmailForgotPassword(userEmail);
    }


    @Override
    public void resetPassword(UserResetPasswordRequest userResetPasswordRequest) {
        String userEmail = userResetPasswordRequest.getUserEmail();
        String captcha = userResetPasswordRequest.getCaptcha();
        String newPassword = userResetPasswordRequest.getNewPassword();
        String confirmPassword = userResetPasswordRequest.getConfirmPassword();

        //1.校验邮箱是否合法
        ThrowUtils.throwIf(!ReUtil.isMatch(RegexPool.EMAIL,userEmail), ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        //3.校验密码长度
        ThrowUtils.throwIf(newPassword.length() < 8||confirmPassword.length()<8, ErrorCode.PARAMS_ERROR,"密码长度不能小于8位");
        //4.校验
        ThrowUtils.throwIf(!newPassword.equals(confirmPassword), ErrorCode.PARAMS_ERROR,"两次密码不一致");
        //5.校验验证码
        String key= RedisConstant.EMAIL.FORGOT+userEmail;
        if (!verifyCaptchaManager.verifyForgotCaptchaOk(key,captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        //6.更新密码
        String encryptPassword = DigestUtils.md5DigestAsHex((newPassword + SALT).getBytes());
        boolean result = this.update(new UpdateWrapper<UserInfo>().lambda().eq(UserInfo::getUserEmail,userEmail).set(UserInfo::getPassword,encryptPassword));
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //7.删除验证码
        verifyCaptchaManager.removeCaptcha(key);
    }

    @Override
    public void userLogout() {
        //清除登录态
        stringRedisTemplate.delete(USER_LOGIN_STATE+StpKit.USER.getLoginIdAsString());
        StpKit.USER.logout();
        StpKit.SPACE.logout();
    }

    @Override
    public LoginUserDetailVO getLoginUserDetail() {
        long userId= StpKit.USER.getLoginIdAsLong();
        String jsonStr = stringRedisTemplate.opsForValue().get(USER_LOGIN_STATE + StpKit.USER.getLoginIdAsString());

        if (StrUtil.isBlank(jsonStr)){
            UserInfo userInfo = this.getById(userId);
            SaTokenInfo tokenInfo = StpKit.USER.getTokenInfo();
            stringRedisTemplate.opsForValue()
                    .set(USER_LOGIN_STATE+userInfo.getId(), JSONUtil.toJsonStr(userInfo)
                            , tokenInfo.getTokenTimeout(), TimeUnit.SECONDS);
            return BeanUtil.copyProperties(userInfo,LoginUserDetailVO.class);
        }
        return JSONUtil.toBean(jsonStr,LoginUserDetailVO.class);
    }


    @Override
    public String uploadAvatar(MultipartFile avatarFile) {
        long userId = StpKit.USER.getLoginIdAsLong();
        //1. 上传地址前缀 avatar/用户id/图片名称
        String pathPrefix="avatar/"+userId+"/";
        //2.上头图片
        UploadPictureResult uploadPictureResult = pictureFileUpload.uploadPicture(avatarFile, pathPrefix,false);
        //3.设置用户图片
        String avatarUrl = uploadPictureResult.getOriginUrl();
        ThrowUtils.throwIf(StrUtil.isBlank(avatarUrl),ErrorCode.OPERATION_ERROR,"上传头像失败");
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setUserAvatar(avatarUrl);
        boolean result = this.updateById(userInfo);
        ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR,"头像更新失败");
        //4.删除缓存
        stringRedisTemplate.delete(USER_LOGIN_STATE+StpKit.USER.getLoginIdAsString());
        //5.返回图片地址
        return avatarUrl;
    }

    @Override
    public void editUserInfo(UserEditRequest userEditRequest) {
        UserInfo userInfo = this.getById(userEditRequest.getId());
        ThrowUtils.throwIf(userInfo==null,ErrorCode.NOT_FOUND_ERROR,"用户不存在");
         userInfo = BeanUtil.copyProperties(userEditRequest, UserInfo.class);
         boolean result = this.updateById(userInfo);
         if (!result){
             throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户信息修改失败");
         }
         //查询一次 更新redis中的用户信息
        userInfo=this.getById(userInfo.getId());
        SaTokenInfo tokenInfo = StpKit.USER.getTokenInfo();
        stringRedisTemplate.opsForValue()
                .set(USER_LOGIN_STATE+userInfo.getId(), JSONUtil.toJsonStr(userInfo)
                        , tokenInfo.getTokenTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public UserDetailVO getUserDetailById(Long userId) {
        String key= USER_LOGIN_STATE+userId;
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        //1.首先查看redis中是否有该用户
        UserInfo userInfo;
        if (jsonStr==null){
            //2.查看数据库中是否有该用户
            userInfo = this.getById(userId);
            ThrowUtils.throwIf(userInfo==null,ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }else {
            userInfo = JSONUtil.toBean(jsonStr, UserInfo.class);
            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(userInfo),7, TimeUnit.DAYS);
        }
        return BeanUtil.copyProperties(userInfo,UserDetailVO.class);
    }

    @Override
    public void deleteUserById(Long id) {
        UserInfo userInfo = this.getById(id);
        if (userInfo==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        boolean result = this.removeById(id);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户信息删除失败");
        }
    }


    @Override
    public void updateUserById(UserUpdateRequest userUpdateRequest) {
        UserInfo userInfo = this.getById(userUpdateRequest.getId());
        ThrowUtils.throwIf(userInfo==null,ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        userInfo = BeanUtil.copyProperties(userUpdateRequest, UserInfo.class);
        boolean result = this.updateById(userInfo);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户信息修改失败");
        }
    }

    @Override
    public PageVO<UserVO> getUserPageListAsManage(UserQueryRequest userQueryRequest) {
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();

        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper = getLambdaQueryWrapper(userQueryRequest);
        Page<UserInfo> page = this.page(new Page<>(current, pageSize), lambdaQueryWrapper);
        PageVO<UserVO> userVOPageVO = new PageVO<>(
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
        return userVOPageVO;
    }


    /**
     * 构造查询参数
     * @param userQueryRequest
     * @return
     */
    public LambdaQueryWrapper<UserInfo> getLambdaQueryWrapper(UserQueryRequest userQueryRequest) {
        Long userId = userQueryRequest.getId();
        String username = userQueryRequest.getUsername();
        String userEmail = userQueryRequest.getUserEmail();
        String userPhone = userQueryRequest.getUserPhone();
        Integer userSex = userQueryRequest.getUserSex();
        Long vipNumber = userQueryRequest.getVipNumber();
        String userRole = userQueryRequest.getUserRole();
        Integer isVip = userQueryRequest.getIsVip();
        Integer isDisabled = userQueryRequest.getIsDisabled();

        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(ObjUtil.isNotEmpty(userId), UserInfo::getId, userId)
                .like(StrUtil.isNotBlank(username), UserInfo::getUsername, username)
                .like(StrUtil.isNotBlank(userEmail), UserInfo::getUserEmail, userEmail)
                .like(StrUtil.isNotBlank(userPhone), UserInfo::getUserPhone, userPhone)
                .eq(ObjUtil.isNotEmpty(userSex), UserInfo::getUserSex, userSex)
                .eq(ObjUtil.isNotEmpty(vipNumber), UserInfo::getVipNumber, vipNumber)
                .eq(StrUtil.isNotBlank(userRole), UserInfo::getUserRole, userRole)
                .eq(ObjUtil.isNotEmpty(isVip), UserInfo::getIsVip, isVip)
                .eq(ObjUtil.isNotEmpty(isDisabled), UserInfo::getIsDisabled, isDisabled);

                //字段排序
                //1.todo function sortField 暂时都采用时间排序 2.考虑多个字段同时排序
        if ("desc".equals(sortOrder)) {
            lambdaQueryWrapper.orderByDesc(UserInfo::getCreateTime);
        } else {
            lambdaQueryWrapper.orderByAsc(UserInfo::getCreateTime);
        }
        return lambdaQueryWrapper;
    }
}







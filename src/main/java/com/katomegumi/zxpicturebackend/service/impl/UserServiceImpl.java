package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.controller.UserController;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.model.dto.user.UserQueryRequest;
import com.katomegumi.zxpicturebackend.model.dao.entity.User;
import com.katomegumi.zxpicturebackend.model.enums.UserRoleEnum;
import com.katomegumi.zxpicturebackend.model.vo.LoginUserVO;
import com.katomegumi.zxpicturebackend.model.vo.UserVO;
import com.katomegumi.zxpicturebackend.service.UserService;
import com.katomegumi.zxpicturebackend.model.dao.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.katomegumi.zxpicturebackend.core.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author lirui
 * @description 针对表【tb_user(用户)】的数据库操作Service实现
 * @createDate 2025-02-04 18:59:15
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private UserController userController;

    @Override
    public Long register(String userAccount, String userPassword, String confirmPassword) {

        if (StrUtil.hasBlank(userAccount, userPassword,confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if (userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }

        if (userPassword.length() < 8 || confirmPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        if (!(userPassword.equals(confirmPassword))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入一样的密码");
        }

        QueryWrapper<User> query = new QueryWrapper<User>().eq("userAccount", userAccount);
        long count = this.count(query);

        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号被占用了");
        }
        String password = getEncryptPassword(userPassword);

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(password);
        user.setUserName("无名");

        boolean result = this.save(user);

        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        //这里有主键回显
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
        .eq("userAccount", userAccount)
        .eq("userPassword", encryptPassword);

        User user = this.baseMapper.selectOne(queryWrapper);

        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        //采用sa-TOKEN 这里是便于空间用户权限使用
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        final String SALT = "zx";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object objUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) objUser;
        if (user == null||user.getId()==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().eq("id", user.getId());
        user = userMapper.selectOne(queryWrapper);
        return user;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user){
        if (user == null) {
            return null;
        }
       return BeanUtil.copyProperties(user, LoginUserVO.class);

    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user){
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }
    @Override
    public List<UserVO> getUserVOList(List<User> userList){
        if (userList.isEmpty()) {
           return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user!=null&&user.getUserRole().equals(UserRoleEnum.ADMIN.getValue());
    }

}





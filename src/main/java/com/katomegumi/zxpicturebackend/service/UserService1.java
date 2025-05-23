package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.katomegumi.zxpicturebackend.model.dto.user.UserQueryRequest;
import com.katomegumi.zxpicturebackend.model.dao.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.model.vo.user.LoginUserDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lirui
* @description 针对表【tb_user(用户)】的数据库操作Service
* @createDate 2025-02-04 18:59:15
*/
public interface UserService1 extends IService<User> {


    /**
     * 用户注册
     * @param username
     * @param password
     * @param confirmPassword
     * @return
     */
    Long register(String username, String password, String confirmPassword);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserDetailVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 密码加密
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 转换User为UserVO
     * @param user
     * @return
     */
    LoginUserDetailVO getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * user转换为 userVO
     * @param user
     * @return
     */
    UserDetailVO getUserVO(User user);

    /**
     * 批量转换 user转换为 userVO
     * @param users
     * @return
     */
    List<UserDetailVO> getUserVOList(List<User> users);

    /**
     * 通过传来的 直接获取queryWrapper对象 实现查询
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);


}

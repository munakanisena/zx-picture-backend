package com.katomegumi.zxpicturebackend.controller;



import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.katomegumi.zxpicturebackend.core.annotation.AuthCheck;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.req.DeleteRequest;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;

import com.katomegumi.zxpicturebackend.core.constant.UserConstant;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;

import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.model.dto.user.*;
import com.katomegumi.zxpicturebackend.model.dao.entity.User;
import com.katomegumi.zxpicturebackend.model.vo.LoginUserVO;
import com.katomegumi.zxpicturebackend.model.vo.UserVO;
import com.katomegumi.zxpicturebackend.service.UserService;

import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {

        @Resource
        @Lazy
        private UserService userService;

        /**
         * 用户注册
         * @param userRegisterRequest
         * @return
         */
        @PostMapping("/register")
        public BaseResponse<Long> UserRegister(@RequestBody UserRegisterRequest userRegisterRequest) {

                ThrowUtils.throwIf(userRegisterRequest==null, ErrorCode.PARAMS_ERROR);

                String userAccount = userRegisterRequest.getUserAccount();
                String userPassword = userRegisterRequest.getUserPassword();
                String confirmPassword = userRegisterRequest.getConfirmPassword();

                Long id= userService.register(userAccount,userPassword,confirmPassword);
                return ResultUtils.success(id);

        }

        /**
         * 用户登录
         * @param userLoginRequest
         * @param request
         * @return
         */
        @PostMapping("/login")
        public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
                ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
                String userAccount = userLoginRequest.getUserAccount();
                String userPassword = userLoginRequest.getUserPassword();
                LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
                return ResultUtils.success(loginUserVO);
        }

        /**
         * 获取登录用户
         * @param request
         * @return
         */
        @GetMapping("/get/login")
        public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
                User loginUser = userService.getLoginUser(request);
                return ResultUtils.success(userService.getLoginUserVO(loginUser));
        }

        /**
         * 用户注销(退出)
         * @param request
         * @return
         */
        @PostMapping("/logout")
        public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
                ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
                boolean result = userService.userLogout(request);
                return ResultUtils.success(result);
        }
        /**
         * 创建用户
         */
        @PostMapping("/add")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
                ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);


                User user = BeanUtil.copyProperties(userAddRequest, User.class);
                // 默认密码 12345678
                final String DEFAULT_PASSWORD = "12345678";
                String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
                user.setUserPassword(encryptPassword);
                boolean result = userService.save(user);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                return ResultUtils.success(user.getId());
        }

        /**
         * 根据 id 获取用户（仅管理员）
         */
        @GetMapping("/get")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<User> getUserById(long id) {
                ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
                User user = userService.getById(id);
                ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
                return ResultUtils.success(user);
        }

        /**
         * 根据 id 获取包装类
         */
        @GetMapping("/get/vo")
        public BaseResponse<UserVO> getUserVOById(long id) {
                BaseResponse<User> response = getUserById(id);
                User user = response.getData();
                return ResultUtils.success(userService.getUserVO(user));
        }

        /**
         * 删除用户
         */
        @PostMapping("/delete")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
                if (deleteRequest == null || deleteRequest.getId() <= 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR);
                }
                boolean b = userService.removeById(deleteRequest.getId());
                return ResultUtils.success(b);
        }

        /**
         * 更新用户
         */
        @PostMapping("/update")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
                if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR);
                }

                User user= BeanUtil.copyProperties(userUpdateRequest, User.class);
                boolean result = userService.updateById(user);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                return ResultUtils.success(true);
        }

        /**
         * 分页获取用户封装列表（仅管理员）
         *
         * @param userQueryRequest 查询请求参数
         */
        @PostMapping("/list/page/vo")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
                ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
                long current = userQueryRequest.getCurrent();
                long pageSize = userQueryRequest.getPageSize();
                Page<User> userPage = userService.page(new Page<>(current, pageSize),
                        userService.getQueryWrapper(userQueryRequest));
                Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
                List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
                userVOPage.setRecords(userVOList);
                return ResultUtils.success(userVOPage);
        }




}

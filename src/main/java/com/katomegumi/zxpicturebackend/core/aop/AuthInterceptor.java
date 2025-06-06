package com.katomegumi.zxpicturebackend.core.aop;

import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.core.annotation.AuthCheck;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.constant.UserConstant;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.enums.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 通过springboot-aop切面 加上注解 进行身份校验
 * @author Megumi
 */
@Component
@Slf4j
@Aspect
@RequiredArgsConstructor
public class AuthInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 进行权限效验
     * @param joinPoint
     * @param authCheck
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint,AuthCheck authCheck) throws Throwable {

        if (authCheck.mustLogin()){
            if (!StpKit.USER.isLogin()){
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
            };
        }

        String mustRole = authCheck.mustRole();
        //获取当前用户
        String jsonStr = stringRedisTemplate.opsForValue().get(UserConstant.USER_LOGIN_STATE + StpKit.USER.getLoginIdAsLong());
        UserInfo userInfo = JSONUtil.toBean(jsonStr, UserInfo.class);

        UserRoleEnum mustRoleEnum = UserRoleEnum.getUserRoleEnum(mustRole);

        //如果为空 说明无需权限直接放行
        if (mustRoleEnum==null){
            return joinPoint.proceed();
        }

        //获取用户的枚举常量
        UserRoleEnum userRoleEnum = UserRoleEnum.getUserRoleEnum(userInfo.getUserRole());

        // 要求必须有管理员权限，但用户没有管理员权限，拒绝
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}

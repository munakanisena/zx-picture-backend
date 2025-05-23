package com.katomegumi.zxpicturebackend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限效验注解
 * @author Megumi
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 是否必须登录
     */
    boolean mustLogin() default true;

    /**
     * 必须为某个角色
     */
    String mustRole() default "";
}

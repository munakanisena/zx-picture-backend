package com.katomegumi.zxpicturebackend.manager.auth.StpKit;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 * 面对多账号权限认证 可以自行声明多个会话
 * 添加 @Component 注解的目的是确保静态属性 SPACE_TYPE 和 USER_TYPE 被初始化
 *
 * @author Megumi
 */
@Component
public class StpKit {

    public static final String SPACE_TYPE = "space";

    public static final String USER_TYPE = "user";

    /**
     * 默认原生会话对象，项目中目前没使用到
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    /**
     * 用户 会话对象，管理 userInfo 表所有账号的登录、权限认证
     */
    public static final StpLogic USER = new StpLogic(USER_TYPE) {
        @Override
        public String splicingKeyTokenName() {
            return super.splicingKeyTokenName() + "-user";
        }
    };

    /**
     * 空间 会话对象，管理 SpaceInfo 表所有账号的登录、权限认证
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE) {
        @Override
        public String splicingKeyTokenName() {
            return super.splicingKeyTokenName() + "-space";
        }
    };


}
package com.katomegumi.zxpicturebackend.manager.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.manager.auth.model.SpaceUserAuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * 自定义权限加载接口实现类 最好只使用一个获取权限码的方法 避免混乱
 *
 * @author Megumi
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StpInterfaceImpl implements StpInterface {
    /**
     * 返回当前 用户的所有权限码
     *
     * @param loginId   登录id
     * @param loginType 登录类型(那个登录体系)
     * @return 权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 判断 loginType，仅对类型为 "space" 进行权限校验
        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            return Collections.emptyList();
        }
        //先判断公共的上传接口 因为此时可能还没有进行空间的登录
        SpaceUserAuthContext authContext = getSpaceUserAuthContextByRequest();
        Long contextSpaceId = authContext.getSpaceId();
        //如果没传空间
        if (contextSpaceId == null) {
            return Collections.emptyList();
        }

        SaSession session = StpKit.SPACE.getSession();
        Long spaceId = session.getModel("spaceId", Long.class);
        if (!spaceId.equals(contextSpaceId)) {
            log.error("用户 {} 尝试操作空间 (ID: {})，但当前会话中记录的空间 (ID: {}) 不一致。", loginId, contextSpaceId, spaceId);
            return Collections.emptyList();
        }
        //否则返回
        return session.getModel("permissions", List.class);
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验) 本项目不适用角色方面的校验
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }


    /**
     * 根据路径 获取所需要的对应参数
     *
     * @return SpaceUserAuthContext
     */
    private SpaceUserAuthContext getSpaceUserAuthContextByRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authRequest;
        //区分get post 请求
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        return authRequest;
    }
}


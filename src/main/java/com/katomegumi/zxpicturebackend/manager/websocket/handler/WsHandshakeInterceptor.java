package com.katomegumi.zxpicturebackend.manager.websocket.handler;

import cn.hutool.core.util.ObjectUtil;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.enums.SpaceTypeEnum;
import com.katomegumi.zxpicturebackend.service.PictureService;
import com.katomegumi.zxpicturebackend.service.SpaceService;
import com.katomegumi.zxpicturebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Megumi
 * @description websocket握手拦截器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WsHandshakeInterceptor implements HandshakeInterceptor {


    private static final String ATTR_USER_INFO = "userInfo";
    private static final String ATTR_PICTURE_ID = "pictureId";
    private final UserService userService;
    private final PictureService pictureService;
    private final SpaceService spaceService;

    /**
     * 在握手之前执行该方法，判断是否允许连接，返回true表示允许(做权限校验)
     *
     * @param request    http请求
     * @param response   http响应
     * @param wsHandler  websocket处理器
     * @param attributes websocket会话属性
     */
    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {
        // 1. 前置类型检查
        if (!(request instanceof ServletServerHttpRequest)) {
            log.warn("请求不是 ServletServerHttpRequest 实例，拒绝握手。");
            return false;
        }

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        //获取图片id
        String pictureId = servletRequest.getParameter("pictureId");
        if (pictureId == null) {
            log.error("缺少图片参数，拒绝握手");
            return false;
        }
        PictureInfo pictureInfo = pictureService.getById(pictureId);
        if (pictureInfo == null) {
            log.error("图片不存在");
            return false;
        }

        UserInfo userInfo = userService.getCurrentUserInfo();
        if (ObjectUtil.isEmpty(userInfo)) {
            log.error("用户未登录");
            return false;
        }

        //校验图片所属空间
        Long spaceId = pictureInfo.getSpaceId();
        if (spaceId == null) {
            log.error("空间id不存在");
            return false;
        } else {
            SpaceInfo spaceInfo = spaceService.getById(spaceId);
            if (ObjectUtil.isEmpty(spaceInfo)) {
                log.error("空间不存在");
                return false;
            }
            if (!spaceInfo.getSpaceType().equals(SpaceTypeEnum.TEAM.getKey())) {
                log.error("不是团队空间");
                return false;
            }
        }
        //补充 websocket会话参数
        attributes.put(ATTR_USER_INFO, userInfo);
        attributes.put(ATTR_PICTURE_ID, Long.valueOf(pictureId));
        return true;
    }

    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, Exception exception) {

    }
}

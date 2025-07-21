package com.katomegumi.zxpicturebackend.manager.websocket.util;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Megumi
 * @description : 图片编辑广播
 * @createDate : 2025/7/17 上午11:01
 */
@Component
public class PictureEditBroadcaster {

    private final Map<Long, Set<WebSocketSession>> pictureEditSessions = new ConcurrentHashMap<>();

    /**
     * 写入会话集合
     *
     * @param pictureId 图片 id
     * @param session   会话
     */
    public void addSession(Long pictureId, WebSocketSession session) {
        pictureEditSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureEditSessions.get(pictureId).add(session);
    }

    /**
     * 删除会话集合
     *
     * @param pictureId 图片 id
     * @param session   会话
     */
    public void removeSession(Long pictureId, WebSocketSession session) {
        Set<WebSocketSession> sessionSet = pictureEditSessions.get(pictureId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                pictureEditSessions.remove(pictureId);
            }
        }
    }

    /**
     * 广播全部用户(除了自己)
     *
     * @param pictureId                  图片id
     * @param pictureEditResponseMessage 传递的消息
     * @param excludeSession             排除的session
     * @throws Exception 异常
     */
    public void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws Exception {
        //拿到所有当前正在编辑的用户会话
        Set<WebSocketSession> sessions = pictureEditSessions.get(pictureId);

        if (CollUtil.isNotEmpty(sessions)) {
            // 这里需要配置序列化 : 将 Long 类型转为 String，解决丢失精度问题
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            // 支持 long 基本类型
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);
            // 序列化为 JSON 字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);

            //逐个发送消息
            for (WebSocketSession session : sessions) {
                // 排除掉的 session 不发送
                if (excludeSession != null && excludeSession.equals(session)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 广播给所有用户
     *
     * @param pictureId                  图片id
     * @param pictureEditResponseMessage 需要传递的消息
     * @throws Exception
     */
    public void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

}


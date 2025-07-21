package com.katomegumi.zxpicturebackend.manager.websocket.strategy;

import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author : Megumi
 * @description :  策略模式接口
 * @createDate : 2025/7/17 上午10:38
 */
public interface PictureEditMessageStrategy {

    /**
     * 处理图片编辑消息,并且广播
     *
     * @param requestMessage 请求消息
     * @param pictureId      当前编辑的图片id
     * @param session        当前会话信息
     * @param userInfo       当前会话用户
     */
    void handle(PictureEditRequestMessage requestMessage, Long pictureId, WebSocketSession session, UserInfo userInfo) throws Exception;
}

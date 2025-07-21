package com.katomegumi.zxpicturebackend.manager.websocket.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.katomegumi.zxpicturebackend.manager.websocket.model.enums.PictureEditMessageTypeEnum;
import com.katomegumi.zxpicturebackend.manager.websocket.strategy.PictureEditMessageStrategy;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author : Megumi
 * @description : 错误消息策略
 * @createDate : 2025/7/17 上午11:18
 */
@Component
public class ErrorMessageStrategy implements PictureEditMessageStrategy {

    /**
     * 处理消息编辑错误
     *
     * @param requestMessage 请求消息
     * @param pictureId      当前编辑的图片id
     * @param session        当前会话信息
     * @param userInfo       当前会话用户
     */
    @Override
    public void handle(PictureEditRequestMessage requestMessage, Long pictureId, WebSocketSession session, UserInfo userInfo) throws Exception {
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.ERROR.getKey());
        responseMessage.setMessage(PictureEditMessageTypeEnum.ERROR.getLabel());
        responseMessage.setUserDetailVO(BeanUtil.copyProperties(userInfo, UserDetailVO.class));
        //直接将错误信息传给当前编辑者
        session.sendMessage(new TextMessage(JSONUtil.toJsonStr(responseMessage)));
    }
}


package com.katomegumi.zxpicturebackend.manager.websocket.disruptor;

import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * 队列消息事件
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private UserInfo user;

    /**
     * 图片 id
     */
    private Long pictureId;

}

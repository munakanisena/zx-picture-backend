package com.katomegumi.zxpicturebackend.manager.websocket.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.katomegumi.zxpicturebackend.manager.websocket.model.enums.PictureEditMessageTypeEnum;
import com.katomegumi.zxpicturebackend.manager.websocket.strategy.PictureEditMessageStrategy;
import com.katomegumi.zxpicturebackend.manager.websocket.util.PictureEditBroadcaster;
import com.katomegumi.zxpicturebackend.manager.websocket.util.UserEditStatusManager;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author : Megumi
 * @description : 退出编辑策略
 * @createDate : 2025/7/17 上午11:21
 */
@Component
@RequiredArgsConstructor
public class ExitEditMessageStrategy implements PictureEditMessageStrategy {

    private final UserEditStatusManager userEditStatusManager;

    private final PictureEditBroadcaster pictureEditBroadcaster;

    @Override
    public void handle(PictureEditRequestMessage requestMessage, Long pictureId, WebSocketSession session, UserInfo userInfo) throws Exception {
        Long editingUserId = userEditStatusManager.getEditingUser(pictureId);
        if (editingUserId != null && editingUserId.equals(userInfo.getId())) {
            //删除编辑状态
            userEditStatusManager.removeEditingUser(pictureId);
            //构造编辑信息
            PictureEditResponseMessage responseMessage = BeanUtil.toBean(requestMessage, PictureEditResponseMessage.class);
            PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.getEnumByKey(responseMessage.getType());
            if (pictureEditMessageTypeEnum == null) {
                return;
            }
            responseMessage.setMessage(String.format("%s%s", userInfo.getName(), pictureEditMessageTypeEnum.getLabel()));
            responseMessage.setUserDetailVO(BeanUtil.copyProperties(userInfo, UserDetailVO.class));
            pictureEditBroadcaster.broadcastToPicture(pictureId, responseMessage, session);
        }
    }
}


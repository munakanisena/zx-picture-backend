package com.katomegumi.zxpicturebackend.manager.websocket.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.katomegumi.zxpicturebackend.manager.websocket.model.enums.PictureEditActionEnum;
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
 * @description : 编辑图片消息处理策略
 * @createDate : 2025/7/17 上午11:22
 */
@Component
@RequiredArgsConstructor
public class EditActionMessageStrategy implements PictureEditMessageStrategy {

    private final UserEditStatusManager userEditStatusManager;

    private final PictureEditBroadcaster pictureEditBroadcaster;

    @Override
    public void handle(PictureEditRequestMessage requestMessage, Long pictureId, WebSocketSession session, UserInfo userInfo) throws Exception {
        //当前图片是否正在编辑
        if (!userEditStatusManager.isEditing(pictureId)) {
            return;
        }
        //如果编辑类型错误
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByKey(requestMessage.getEditAction());
        if (pictureEditActionEnum == null) {
            return;
        }
        //当前用户是否为编辑者
        Long editingUserId = userEditStatusManager.getEditingUser(pictureId);
        if (editingUserId != null && editingUserId.equals(userInfo.getId())) {
            PictureEditResponseMessage responseMessage = BeanUtil.toBean(requestMessage, PictureEditResponseMessage.class);
            responseMessage.setMessage(String.format("%s执行%s", userInfo.getName(), pictureEditActionEnum.getLabel()));
            responseMessage.setUserDetailVO(BeanUtil.copyProperties(userInfo, UserDetailVO.class));
            pictureEditBroadcaster.broadcastToPicture(pictureId, responseMessage, session);
        }
    }
}


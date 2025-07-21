package com.katomegumi.zxpicturebackend.manager.websocket.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Megumi
 * @description : 用户编辑状态管理器
 * @createDate : 2025/7/17 上午10:49
 */
@Component
public class UserEditStatusManager {

    private final Map<Long, Long> userEditStatusManager = new ConcurrentHashMap<>();

    public boolean isEditing(Long pictureId) {
        return userEditStatusManager.containsKey(pictureId);
    }

    public void setEditingUser(Long pictureId, Long userId) {
        userEditStatusManager.put(pictureId, userId);
    }

    public Long getEditingUser(Long pictureId) {
        return userEditStatusManager.get(pictureId);
    }

    public void removeEditingUser(Long pictureId) {
        userEditStatusManager.remove(pictureId);
    }
}


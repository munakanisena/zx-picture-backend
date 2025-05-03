package com.katomegumi.zxpicturebackend.manager.websocket;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.katomegumi.zxpicturebackend.manager.websocket.disruptor.PictureEditEventProducer;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditActionEnum;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.katomegumi.zxpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.katomegumi.zxpicturebackend.model.dao.entity.User;
import com.katomegumi.zxpicturebackend.model.vo.UserVO;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * websocket  消息处理器
 */
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    //当前正在编辑的用户
    private final Map<Long,Long> pictureEditingUsers=new ConcurrentHashMap<>();

    //websocket  一个图片几个人在编辑  key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions=new ConcurrentHashMap<>();

    @Resource
    private PictureEditEventProducer pictureEditEventProducer;

    //链接成功后
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //写入会话
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User user  = (User) session.getAttributes().get("user");
        pictureSessions.putIfAbsent(pictureId,ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        //构造响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        UserVO userVo = BeanUtil.toBean(user, UserVO.class);
        String message=String.format("%s用户加入了编辑",user.getUserName());
        pictureEditResponseMessage.setUser(userVo);
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    //接收信息后处理
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //获取消息的负载 将json数据转为java对象
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        String type = pictureEditRequestMessage.getType();

        //获取session 属性(对象)
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User user  = (User) session.getAttributes().get("user");
        //利用队列 并行执行
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage,session,user,pictureId);



        //获取对应的枚举类
        //PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.valueOf(type);
//        //根据编辑的类型发送消息
//        switch (pictureEditMessageTypeEnum){
//            case ENTER_EDIT:
//                handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
//                break;
//            case EDIT_ACTION:
//                handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
//                break;
//            case EXIT_EDIT:
//                handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
//                break;
//                //全都不符合 响应错误
//            default:
//                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
//                pictureEditResponseMessage.setMessage("消息类型错误");
//                pictureEditResponseMessage.setUser(BeanUtil.toBean(user, UserVO.class));
//                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
//                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
//        }
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        User user = (User) attributes.get("user");
        //移除当前登录
        handleExitEditMessage(null,session,user,pictureId);
        //移除会话用户
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if(webSocketSessions!=null){
            webSocketSessions.remove(session);
            if (webSocketSessions.isEmpty()){
                pictureSessions.remove(pictureId);
            }
        }
        // 响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("%s离开编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(BeanUtil.toBean(user, UserVO.class));
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }

    //开始编辑
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        //没人编辑菜进入
        if (!pictureEditingUsers.containsKey(pictureId)) {
         //设置当前为编辑用户
        pictureEditingUsers.put(pictureId,user.getId());
        //构造响应体
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setUser(BeanUtil.toBean(user, UserVO.class));
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
        String message=String.format("%s用户开始编辑",user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        broadcastToPicture(pictureId,pictureEditResponseMessage);
    }

    }

    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (pictureEditActionEnum == null) {
            return;
        }
        //如果一致才允许编辑
        if (pictureEditingUsers.get(pictureId).equals(user.getId())&&pictureEditingUsers.get(pictureId)!=null) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setUser(BeanUtil.toBean(user, UserVO.class));
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message=String.format("%s用户执行%s",user.getUserName(),pictureEditActionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(pictureEditActionEnum.getValue());
            broadcastToPicture(pictureId,pictureEditResponseMessage,session);
        }
    }

    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        Long editUser = pictureEditingUsers.get(pictureId);
        //说明操作用户相同
        if (editUser!=null&&editUser.equals(user.getId())) {
            //移除用户当前编辑状态
            pictureEditingUsers.remove(pictureId);
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setUser(BeanUtil.toBean(user, UserVO.class));
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message=String.format("%s用户退出编辑",user.getUserName());
            pictureEditResponseMessage.setMessage(message);

            broadcastToPicture(pictureId,pictureEditResponseMessage,session);
        }
    }



    /**
     * 发送团队协作消息
     * @param pictureId
     * @param pictureEditResponseMessage
     * @param exclude
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage,WebSocketSession exclude ) throws Exception {
        Set<WebSocketSession> userSessions = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(userSessions)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
            objectMapper.registerModule(module);
            // 序列化为 JSON 字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : userSessions) {
                if (session.equals(exclude) && exclude!=null){
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }

        }
    }
    // 全部广播

    /**
     * 无需排除
     * @param pictureId
     * @param pictureEditResponseMessage
     * @throws Exception
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

}

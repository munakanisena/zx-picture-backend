package com.katomegumi.zxpicturebackend.manager.websocket.strategy;

import com.katomegumi.zxpicturebackend.manager.websocket.model.enums.PictureEditMessageTypeEnum;
import com.katomegumi.zxpicturebackend.manager.websocket.strategy.impl.EditActionMessageStrategy;
import com.katomegumi.zxpicturebackend.manager.websocket.strategy.impl.EnterEditMessageStrategy;
import com.katomegumi.zxpicturebackend.manager.websocket.strategy.impl.ErrorMessageStrategy;
import com.katomegumi.zxpicturebackend.manager.websocket.strategy.impl.ExitEditMessageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Megumi
 * @description : (编辑消息处理)工厂模式
 * @createDate : 2025/7/17 上午11:16
 */
@Component
@RequiredArgsConstructor
public class PictureEditMessageStrategyFactory {

    private final Map<String, PictureEditMessageStrategy> strategies = new HashMap<>();

    private final EnterEditMessageStrategy enterEditMessageStrategy;

    private final ExitEditMessageStrategy exitEditMessageStrategy;

    private final EditActionMessageStrategy editActionMessageStrategy;

    private final ErrorMessageStrategy errorMessageStrategy;

    @PostConstruct
    public void init() {
        strategies.put(PictureEditMessageTypeEnum.ENTER_EDIT.getKey(), enterEditMessageStrategy);
        strategies.put(PictureEditMessageTypeEnum.EDIT_ACTION.getKey(), editActionMessageStrategy);
        strategies.put(PictureEditMessageTypeEnum.EXIT_EDIT.getKey(), exitEditMessageStrategy);
    }

    /**
     * 根据类型获取对应的策略(如果没有 默认错误策略)
     *
     * @param type 类型
     * @return 消息策略
     */
    public PictureEditMessageStrategy getStrategy(String type) {
        return strategies.getOrDefault(type, errorMessageStrategy);
    }
}


package com.katomegumi.zxpicturebackend.core.config;


import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.katomegumi.zxpicturebackend.manager.websocket.disruptor.PictureEditEvent;
import com.katomegumi.zxpicturebackend.manager.websocket.disruptor.PictureEditEventWorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


/**
 * 配置队列
 */
@Configuration
public class PictureEditEventDisruptorConfig {
    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        //设置队列空间
        // ringBuffer 的大小
        int bufferSize = 1024 * 256;
        Disruptor<PictureEditEvent> disruptor = new Disruptor<PictureEditEvent>(
            PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        //设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        //开启队列
        disruptor.start();
        return disruptor;
    }

}

package com.katomegumi.zxpicturebackend.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author : Megumi
 * @description : 线程池配置
 * @createDate : 2025/5/7 下午12:31
 */
@Configuration
public class TreadPoolConfig {

    /**
     * 邮件线程池
     */
    @Bean("emailThreadPool")
    public ThreadPoolTaskExecutor emailTreadPool() {
        //1.创建线程池
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //2.设置线程池参数
        //2.1设置核心线程
        threadPoolTaskExecutor.setCorePoolSize(5);
        //2.2设置最大线程
        threadPoolTaskExecutor.setMaxPoolSize(10);
        //2.3设置队列容量
        threadPoolTaskExecutor.setQueueCapacity(30);
        //2.4设置线程名称
        threadPoolTaskExecutor.setThreadNamePrefix("emailThreadPool-");
        //2.5设置拒绝策略 （表示当线程池和队列都满时，新任务将由调用者线程（即提交任务的线程）来执行）
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //2.6设置 等待所有任务结束后再关闭线程池
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        //3.初始化线程池
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }

    /**
     * cos程池
     */
    @Bean("cosThreadPool")
    public ThreadPoolTaskExecutor clearTreadPool() {
        //1.创建线程池
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //2.设置线程池参数
        //2.1设置核心线程
        threadPoolTaskExecutor.setCorePoolSize(3);
        //2.2设置最大线程
        threadPoolTaskExecutor.setMaxPoolSize(5);
        //2.3设置队列容量
        threadPoolTaskExecutor.setQueueCapacity(30);
        //2.4设置线程名称
        threadPoolTaskExecutor.setThreadNamePrefix("cosThreadPool-");
        //2.5设置拒绝策略 （表示当线程池和队列都满时，新任务将由调用者线程（即提交任务的线程）来执行）
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //2.6设置 等待所有任务结束后再关闭线程池
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        //3.初始化线程池
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}


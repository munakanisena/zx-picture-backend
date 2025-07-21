package com.katomegumi.zxpicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author lirui
 * @description 启动类
 */
@EnableAsync
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@MapperScan("com.katomegumi.zxpicturebackend.model.dao.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) //暴露代理对象
@EnableCaching
public class ZxPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZxPictureBackendApplication.class, args);
    }

}

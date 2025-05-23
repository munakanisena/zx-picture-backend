package com.katomegumi.zxpicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author lirui
 */
@EnableAsync //可以使得方法被异步调用
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class}) //关闭分表
@MapperScan("com.katomegumi.zxpicturebackend.model.dao.mapper")
@CrossOrigin
@EnableAspectJAutoProxy(exposeProxy = true) //暴露代理对象
public class ZxPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZxPictureBackendApplication.class, args);
    }

}

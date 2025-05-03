package com.katomegumi.zxpicturebackend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域处理
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**")
               //允许发送cookie
               .allowCredentials(true)
               //放行哪些域名
               .allowedOriginPatterns("*")
               .allowedHeaders("*")
               .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
               .exposedHeaders("*");
    }
}

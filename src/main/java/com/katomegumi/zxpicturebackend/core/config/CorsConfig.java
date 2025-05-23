package com.katomegumi.zxpicturebackend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域处理 后端不做处理 采用前端代理转发模式 不然需要设置 Cookie的 SameSite 比较麻烦
 * 上线采用 nginx 代理转发
 */
@Deprecated
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**")
               //允许发送cookie
               .allowCredentials(true)
               //放行哪些域名
               .allowedOrigins("http://localhost:5173")
               .allowedHeaders("*")
               .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
               .exposedHeaders("*");
    }
}

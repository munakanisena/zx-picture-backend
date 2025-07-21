package com.katomegumi.zxpicturebackend.core.api.aliyunai.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Megumi
 * @description : 阿里百炼配置
 * @createDate : 2025/6/10 下午7:28
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.bailian")
public class BaiLianConfig {

    private String secretKey;

    // 创建任务地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";


    public String getBearer() {
        return "Bearer " + secretKey;
    }

}


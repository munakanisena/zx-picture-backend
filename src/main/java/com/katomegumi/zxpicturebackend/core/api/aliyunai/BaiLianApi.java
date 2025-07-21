package com.katomegumi.zxpicturebackend.core.api.aliyunai;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.BaiLianConfig;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 参考文档: <a href="https://help.aliyun.com/zh/model-studio/image-scaling-api?spm=a2c4g.11186623.0.0.2c3b90d9rQntD9">...</a>
 *
 * @author Megumi
 * @description: 阿里百炼 API
 * @createDate: 2025/6/10 下午7:27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaiLianApi {

    private final BaiLianConfig baiLianConfig;

    private final OkHttpClient okHttpClient;

    /**
     * 创建任务
     *
     * @param createOutPaintingTaskRequest 请求参数
     * @return 任务ID
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        log.info("阿里百炼扩图任务开始参数:{}", createOutPaintingTaskRequest);
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSONUtil.toJsonStr(createOutPaintingTaskRequest), mediaType);

        Request request = new Request.Builder()
                .url(BaiLianConfig.CREATE_OUT_PAINTING_TASK_URL)
                .header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                .header(Header.AUTHORIZATION.getValue(), baiLianConfig.getBearer())
                .header("X-DashScope-Async", "enable")
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("阿里百炼扩图任务失败:状态码{},信息{}", response.code(), response.message());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败");
            }
            try (ResponseBody body = response.body()) {
                if (ObjectUtil.isNull(body)) {
                    log.debug("阿里百炼扩图任务失败:{}", response.code());
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败返回体为空");
                }
                CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(body.string(), CreateOutPaintingTaskResponse.class);
                if (StrUtil.isNotBlank(createOutPaintingTaskResponse.getCode()) || StrUtil.isNotBlank(createOutPaintingTaskResponse.getMessage())) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败");
                }
                return createOutPaintingTaskResponse;
            }
        } catch (IOException e) {
            log.error("创建阿里云百炼扩图任务失败:{}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败");
        }
    }

    /**
     * 查询任务
     *
     * @param taskId 任务 id
     * @return 扩图任务结果
     */
    public GetOutPaintingTaskResponse queryOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 id 不能为空");
        }
        Request request = new Request.Builder()
                .get()
                .url(String.format(BaiLianConfig.GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION.getValue(), baiLianConfig.getBearer())
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("查询阿里云百炼扩图任务失败:{}", response.code());
            }
            ResponseBody body = response.body();
            if (ObjectUtil.isNull(body)) {
                log.debug("阿里百炼扩图任务失败:{}", response.code());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败返回体为空");
            }
            GetOutPaintingTaskResponse getOutPaintingTaskResponse = JSONUtil.toBean(body.string(), GetOutPaintingTaskResponse.class);
            if (StrUtil.isNotBlank(getOutPaintingTaskResponse.getOutput().getCode()) || StrUtil.isNotBlank(getOutPaintingTaskResponse
                    .getOutput().getMessage())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败");
            }
            return getOutPaintingTaskResponse;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
        }
    }
}

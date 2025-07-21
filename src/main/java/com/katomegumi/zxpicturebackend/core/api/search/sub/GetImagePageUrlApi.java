package com.katomegumi.zxpicturebackend.core.api.search.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 调用 百度以图搜图接口获取 html页面 步骤1
 */
@Slf4j
public class GetImagePageUrlApi {

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        String result = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果 URL：" + result);
    }

    /**
     * 获取图片页面地址
     *
     * @param imageUrl 图片url
     * @return html页面地址
     */
    public static String getImagePageUrl(String imageUrl) {
        // 1. 准备请求参数 请求表单
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        //设置时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        try {
            //发送请求
            HttpResponse response = HttpRequest.post(url)
                    .form(formData)
                    .header("acs-token", RandomUtil.randomString(1))
                    .timeout(5000)
                    .execute();
            //判断响应是否成功
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "请求失败");
            }
            String body = response.body();
            //转换为 json格式 用map存储
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);

            // 3. 处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (BusinessException e) {
            log.error("以图搜图失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

}

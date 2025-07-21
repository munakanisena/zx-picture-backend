package com.katomegumi.zxpicturebackend.core.api.search.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.core.api.search.model.SearchPictureResult;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 提取以图搜图列表(JSON)的图片 步骤三
 *
 * @author Megumi
 */
@Slf4j
public class GetImageListApi {

    public static void main(String[] args) {
        String url = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=4985731586020618930&sign=12672e97cd54acd88139901741263185&tk=d9798&tpl_from=pc&page=1&";
        List<SearchPictureResult> imageList = getImageListApi(url, 1, 10);
        System.out.println("搜索成功" + imageList);
    }

    /**
     * 发送请求获取 html 解析获取图片列表
     *
     * @param requestUrl  请求地址
     * @param randomSeed  随机种子
     * @param searchCount 搜索数量
     * @return 图片列表
     */
    public static List<SearchPictureResult> getImageListApi(String requestUrl, Integer randomSeed, Integer searchCount) {
        //默认10张
        if (searchCount == null) searchCount = 10;
        List<SearchPictureResult> resultList = new ArrayList<>();
        int currentWhileNum = 0;
        //循环次数
        int targetWhileNum = searchCount / 30 + 1;
        while (currentWhileNum < targetWhileNum && resultList.size() < searchCount) {
            if (randomSeed == null) randomSeed = RandomUtil.randomInt(1, 20);
            String URL = requestUrl + "&page=" + randomSeed;
            log.info("请求地址：{}", URL);
            try (HttpResponse response = HttpUtil.createGet(URL).execute()) {
                // 判断响应状态
                if (HttpStatus.HTTP_OK != response.getStatus()) {
                    log.error("搜图失败，响应状态码：{}", response.getStatus());
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜图失败");
                }
                // 解析响应, 处理响应结果
                JSONObject body = JSONUtil.parseObj(response.body());
                if (!body.containsKey("data")) {
                    log.error("搜图失败，未获取到图片数据");
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
                }
                JSONObject data = body.getJSONObject("data");
                if (!data.containsKey("list")) {
                    log.error("搜图失败，未获取到图片数据");
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
                }
                JSONArray baiduResult = data.getJSONArray("list");
                for (Object object : baiduResult) {
                    JSONObject so = (JSONObject) object;
                    SearchPictureResult pictureResult = new SearchPictureResult();
                    pictureResult.setThumbUrl(so.getStr("thumbUrl"));
                    pictureResult.setFromUrl(so.getStr("fromUrl"));
                    resultList.add(pictureResult);
                }
                currentWhileNum++;
            } catch (Exception e) {
                log.error("搜图失败,错误信息:{}", e.getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜图失败");
            } finally {
                randomSeed++;
            }
        }
        return resultList;
    }

}

package com.katomegumi.zxpicturebackend.core.api.imagesearch.sub;

import cn.hutool.http.*;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.core.api.imagesearch.model.ImageSearchResult;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;



import java.util.List;

/**
 *  提取以图搜图列表(JSON)的图片 步骤三
 */
@Slf4j
public class GetImageListApi {

    /**
     * 获取图片列表
     * @param url
     * @return
     */
    public static List<ImageSearchResult> getImageListApi(String url){
        try {
            HttpResponse response = HttpUtil.createGet(url).execute();
            if (response.getStatus()!= HttpStatus.HTTP_OK){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求失败");
            }
            String body = response.body();
            return processResponse(body);
        } catch (BusinessException e) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"以图搜图失败");
    }
}

    /**
     * 获取以图搜图的文件
     * @param body
     * @return
     */
    private static List<ImageSearchResult> processResponse(String body) {
        //转为json格式
        JSONObject entries = new JSONObject(body);
        if (!entries.containsKey("data")) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "数据不存在");
        }
        JSONObject data = entries.getJSONObject("data");
        if (!data.containsKey("list")) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "数据不存在");
        }
        JSONArray list = data.getJSONArray("list");
        //自动匹配 能够将json数据转换为一个java 的数组
        return JSONUtil.toList(list, ImageSearchResult.class);
    }

    public static void main(String[] args) {
        String url="https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=4985731586020618930&sign=12672e97cd54acd88139901741263185&tk=d9798&tpl_from=pc&page=1&";
        List<ImageSearchResult> imageList = getImageListApi(url);
        System.out.println("搜索成功" + imageList);
    }
}

package com.katomegumi.zxpicturebackend.core.api;

import com.katomegumi.zxpicturebackend.core.api.imagesearch.model.ImageSearchResult;
import com.katomegumi.zxpicturebackend.core.api.imagesearch.sub.GetImageFirstUrlApi;
import com.katomegumi.zxpicturebackend.core.api.imagesearch.sub.GetImageListApi;
import com.katomegumi.zxpicturebackend.core.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * java 设计模式 门面模式 (不关注内部的具体实现 一个统一的接口来简化多个接口的调用)
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String url = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String fistUrl = GetImageFirstUrlApi.getImageFirstUrl(url);
        List<ImageSearchResult> imageList = GetImageListApi.getImageListApi(fistUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}

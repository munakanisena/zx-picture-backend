package com.katomegumi.zxpicturebackend.core.api.search;

import com.katomegumi.zxpicturebackend.core.api.search.model.SearchPictureResult;
import com.katomegumi.zxpicturebackend.core.api.search.sub.GetImageFirstUrlApi;
import com.katomegumi.zxpicturebackend.core.api.search.sub.GetImageListApi;
import com.katomegumi.zxpicturebackend.core.api.search.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * java 设计模式 门面模式 (不关注内部的具体实现 一个统一的接口来简化多个接口的调用)
 *
 * @author Megumi
 */
@Slf4j
public class ImageSearchApiFacade {


    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        List<SearchPictureResult> resultList = searchImage(imageUrl, 1, 10);
        System.out.println("结果列表" + resultList);
    }

    /**
     * 搜索图片
     *
     * @param imageUrl 图片url
     * @return 搜索结果
     */
    public static List<SearchPictureResult> searchImage(String imageUrl, Integer randomSeed, Integer searchCount) {
        log.info("开始以图搜图，图片url:{};图片种子数:{},图片搜索数量{}", imageUrl, randomSeed, searchCount);
        String url = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String fistUrl = GetImageFirstUrlApi.getImageFirstUrl(url);
        List<SearchPictureResult> pictureResults = GetImageListApi.getImageListApi(fistUrl, randomSeed, searchCount);
        if (pictureResults.size() > searchCount) {
            pictureResults = pictureResults.subList(0, searchCount);
        }
        log.info("以图搜图结束，结果列表:{}", pictureResults);
        return pictureResults;
    }
}

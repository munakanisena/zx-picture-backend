package com.katomegumi.zxpicturebackend.core.api.capture;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.model.enums.CaptureSourceEnum;
import com.katomegumi.zxpicturebackend.model.vo.picture.CapturePictureResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Megumi
 * @description : 抓取图片管理
 * @createDate : 2025/6/6 下午4:26
 */
@Component
@Slf4j
public class CapturePictureManager {
    //测试
    public static void main(String[] args) {
        CapturePictureManager capturePictureManager = new CapturePictureManager();
        List<CapturePictureResult> capturePictureResults = capturePictureManager
                .capturePicture(CaptureSourceEnum.BING.getUrl(), "风景", 10, 10);
//        System.out.println(capturePictureResults);
        System.out.println(JSONUtil.parse(capturePictureResults));
    }


    /**
     * 批量抓取图片
     *
     * @param captureUrl   抓取源
     * @param searchText   搜索关键词
     * @param captureCount 抓取数量
     * @param randomSeed   随机种子
     * @return 抓取结果
     */
    public List<CapturePictureResult> capturePicture(String captureUrl, String searchText, Integer captureCount, Integer randomSeed) {
        log.info("开始抓取图片:url:{},searchText:{},captureCount:{},randomSeed:{}", captureUrl, searchText, captureCount, randomSeed);
        //1.首先构建地址
        captureUrl = String.format(captureUrl, searchText, randomSeed, captureCount);
        log.info("抓取地址:{}", captureUrl);
        try {
            Document document = Jsoup.connect(captureUrl).get();
            Elements div = document.getElementsByClass("dgControl");
            if (ObjUtil.isNull(div)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
            }
            Elements imgElementList = div.select(".iusc");
            List<CapturePictureResult> capturePictureResultsList = new ArrayList<>();
            for (Element imgElement : imgElementList) {
                try {
                    //获取m 属性(包含图片信息 地址 缩略图等)
                    String m = imgElement.attr("m");
                    JSONObject json = JSONUtil.parseObj(m);
                    //获取原图地址
                    String originUrl = json.getStr("murl");
                    //获取缩略图地址
                    String thumbnailUrl = json.getStr("turl");

                    if (StrUtil.isBlank(originUrl) && StrUtil.isBlank(thumbnailUrl)) {
                        continue;
                    }

                    CapturePictureResult capturePictureResult = new CapturePictureResult();
                    if (StrUtil.isNotBlank(originUrl)) {
                        capturePictureResult.setCaptureUrl(originUrl);
                    } else {
                        capturePictureResult.setCaptureUrl(thumbnailUrl);
                    }
                    //这里暂时使用缩略图代替 网站一般都具有反爬机制
                    capturePictureResult.setHandleCaptureUrl(thumbnailUrl);
                    capturePictureResultsList.add(capturePictureResult);
                } catch (Exception e) {
                    log.error("解析图片失败", e);
                }
            }
            return capturePictureResultsList;
        } catch (IOException e) {
            log.error("抓取源页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "抓取源页面失败");
        }
    }
}


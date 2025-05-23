package com.katomegumi.zxpicturebackend.manager.cos;

import cn.hutool.core.io.FileUtil;
import com.katomegumi.zxpicturebackend.core.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;

import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用的类
 */
@Component
@RequiredArgsConstructor
public class CosManager {


    private final CosClientConfig cosClientConfig;


    private final COSClient cosClient;

    //一些 cos操作

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象（数据万象）
     * <p>
     * <a href="https://cloud.tencent.com/document/product/436/115609">腾讯 COS 数据万象</a>
     *
     * @param key    图片地址
     * @param file   文件
     * @param openWx 开启数据万象; true: 开启; false: 关闭 (是否对图片进行处理)
     */
    public PutObjectResult putPictureObject(String key, File file,boolean openWx) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        //1 表示返回原图信息
        picOperations.setIsPicInfo(1);

        //是否处理图片
        if (openWx){
            //图片规则列表
            List<PicOperations.Rule> rules= new ArrayList<>();
            //压缩图片 成webp格式
            PicOperations.Rule rule = new PicOperations.Rule();
            rule.setBucket(cosClientConfig.getBucket());
            rule.setFileId(FileUtil.mainName(key)+".webp");
            rule.setRule("imageMogr2/format/webp");
            rules.add(rule);

            //图片大于20kb 才有缩略图
            if (file.length()>1024*20){
                //缩放图片 获取缩放图
                PicOperations.Rule thumbnailRule = new PicOperations.Rule();
                thumbnailRule.setBucket(cosClientConfig.getBucket());
                //进行等比缩放 如果图片小于要求 则不缩放
                thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",256,256));
                thumbnailRule.setFileId(FileUtil.mainName(key)+"_thumbnail."+FileUtil.getSuffix(key));
                rules.add(thumbnailRule);
                //设置规则到操作中
            }
            picOperations.setRules(rules);
        }
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }



    /**
     * 删除对象存储的图片
     * @param key 文件资源地址
     * @throws CosClientException
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(),key);
    }
}

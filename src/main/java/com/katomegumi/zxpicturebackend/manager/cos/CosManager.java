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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用的类 那个项目都可以使用
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

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
     * 上传图片(并且解析)
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        PicOperations picOperations = new PicOperations();
        //1 表示返回原图信息
        picOperations.setIsPicInfo(1);

        //图片规则列表
        List<PicOperations.Rule> rules= new ArrayList<>();
        //压缩图片 成webp格式
        String webKey= FileUtil.mainName(key)+".webp";
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setBucket(cosClientConfig.getBucket());
        rule.setFileId(webKey);
        rule.setRule("imageMogr2/format/webp");
        rules.add(rule);

        //缩放图片 获取缩放图
        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        thumbnailRule.setBucket(cosClientConfig.getBucket());
        //进行等比缩放 如果图片小于要求 则不缩放
        thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",128,128));
        String thumbnailKey=FileUtil.mainName(key)+"thumbnail"+FileUtil.getSuffix(key);
        thumbnailRule.setFileId(thumbnailKey);
        rules.add(thumbnailRule);
        //设置规则到操作中
        picOperations.setRules(rules);
        //规则设置在请求中
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象存储的图片
     * @param key
     * @throws CosClientException
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(),key);
    }
}

package com.katomegumi.zxpicturebackend.manager.cos;

import cn.hutool.core.io.FileUtil;
import com.katomegumi.zxpicturebackend.core.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Megumi
 * 腾讯云对象存储服务
 */
@Component
@RequiredArgsConstructor
@Slf4j
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
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucketName(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucketName(), key);
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
    public PutObjectResult putPictureObject(String key, File file, boolean openWx) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucketName(), key, file);
        //设置图片操作选项
        PicOperations picOperations = new PicOperations();
        //1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        //是否处理图片
        if (openWx) {
            //图片规则列表
            List<PicOperations.Rule> rules = new ArrayList<>();
            //压缩图片 webp格式
            PicOperations.Rule rule = new PicOperations.Rule();
            rule.setBucket(cosClientConfig.getBucketName());
            rule.setFileId(FileUtil.mainName(key) + ".webp");
            rule.setRule("imageMogr2/format/webp");
            rules.add(rule);

            //图片大于20kb 才有缩略图
            if (file.length() > 1024 * 20) {
                //缩放图片 获取缩放图
                PicOperations.Rule thumbnailRule = new PicOperations.Rule();
                thumbnailRule.setBucket(cosClientConfig.getBucketName());
                //进行等比缩放 如果图片小于要求 则不缩放
                thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
                thumbnailRule.setFileId(FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key));
                //设置规则到操作中
                rules.add(thumbnailRule);
            }
            picOperations.setRules(rules);
        }
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象存储的图片
     *
     * @param key 文件资源地址
     * @throws CosClientException
     */
    public void deleteObject(String key) throws CosClientException {
        try {
            cosClient.deleteObject(cosClientConfig.getBucketName(), key);
        } catch (CosServiceException e) {
            // 如果是其他错误, 比如参数错误， 身份验证不过等会抛出CosServiceException
            log.error("删除文件失败,错误码:{},消息:{}", e.getErrorCode(), e.getMessage(), e);
        } catch (CosClientException e) {
            // 如果是客户端错误，比如连接不上COS
            log.error("删除文件失败,错误码:{},消息:{}", e.getErrorCode(), e.getMessage(), e);
        }
    }

    /**
     * 批量删除文件(不带版本号, 即bucket未开启多版本)
     * <p>文档
     * <a href="https://cloud.tencent.com/document/product/436/65939#841fe310-bdf8-4789-9bc0-26ea844e316d">...</a>
     *
     * @param keys 对象键列表
     */
    public void batchDeleteObjects(List<String> keys) {
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucketName());
        // 设置要删除的key列表, 最多一次删除1000个
        ArrayList<DeleteObjectsRequest.KeyVersion> keyList = new ArrayList<>();
        // 传入要删除的文件名
        for (String key : keys) {
            keyList.add(new DeleteObjectsRequest.KeyVersion(key));
        }
        deleteObjectsRequest.setKeys(keyList);
        // 批量删除文件
        try {
            DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
            List<DeleteObjectsResult.DeletedObject> deleteObjectResultArray = deleteObjectsResult.getDeletedObjects();
        } catch (MultiObjectDeleteException mde) {
            // 如果部分产出成功部分失败, 返回MultiObjectDeleteException
            List<DeleteObjectsResult.DeletedObject> deleteObjects = mde.getDeletedObjects();
            List<MultiObjectDeleteException.DeleteError> deleteErrors = mde.getErrors();
        } catch (CosServiceException e) {
            // 如果是其他错误, 比如参数错误， 身份验证不过等会抛出CosServiceException
            log.error("批量删除文件失败,错误码:{},消息:{}", e.getErrorCode(), e.getMessage(), e);
        } catch (CosClientException e) {
            // 如果是客户端错误，比如连接不上COS
            log.error("批量删除文件失败,错误码:{},消息:{}", e.getErrorCode(), e.getMessage(), e);
        }
    }
}

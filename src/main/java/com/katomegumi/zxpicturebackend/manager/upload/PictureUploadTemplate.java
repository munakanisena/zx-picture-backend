package com.katomegumi.zxpicturebackend.manager.upload;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.manager.cos.CosManager;
import com.katomegumi.zxpicturebackend.manager.upload.modal.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

/**
 * 上传图片的抽象
 * java设计模版模式
 * @author Megumi
 */
@Slf4j
public abstract class PictureUploadTemplate {

//    @Resource
//    private  CosClientConfig cosClientConfig;

    @Resource
    private  CosManager cosManager;


    /**
     * 上传图片流程
     * @param inputSource 输入源
     * @param uploadPathPrefix 上传路径前缀
     * @param openWx 是否开启图片处理
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource,String uploadPathPrefix,boolean openWx) {
        //1.校验文件
        validPicture(inputSource);
        //2.构造上传地址
        String uuid= RandomUtil.randomString(16);
        //获取原图片名称(包括后缀名) 作为默认  后续设置名称时 需要去除后缀名
        String originFilename = getOriginFileName(inputSource);

        String uploadFilename =String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFilename);

        File file=null;
        try {
            file=File.createTempFile(uploadPath,null);
            //构造文件 对象
            processFile(inputSource,file);
            return uploadPictureToStorage(file,uploadPath,originFilename,openWx);
        }catch (Exception e){
            log.error("文件上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }finally {
            //清理临时文件
            deleteTempFile(file);
        }
    }

    /**
     * 上传图片到COS 并且返回图片信息
     * @return
     */
    public UploadPictureResult uploadPictureToStorage(File file,String uploadPath,String originFilename, boolean openWx){
        PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file, openWx);
        //原图信息
        OriginalInfo originalInfo = putObjectResult.getCiUploadResult().getOriginalInfo();
        if (openWx){
            //处理图片后的信息
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            //取处理结果  是按规则进行排序的 0是压缩图 1是缩略图
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isEmpty(objectList)){
                return buildResult(originalInfo,originFilename,null,null);
            }
            CIObject compressObject = objectList.get(0);
            //表示有 缩略图处理
            if (objectList.size() > 1){
                CIObject thumbnailObject = objectList.get(1);
                return buildResult(originalInfo,originFilename,compressObject,thumbnailObject);
            }
            return buildResult(originalInfo,originFilename,compressObject,null);

        }else {
            return  buildResult(originalInfo,originFilename,null,null);
        }
    }


    /**
     * 获取 图片上传结果
     * @param originalInfo 原图信息
     * @param originFilename 原图名称(有后缀)
     * @param compressObject 压缩图信息
     * @param thumbnailObject 缩略图信息
     * @return
     */
    private UploadPictureResult buildResult(OriginalInfo originalInfo, String originFilename, CIObject compressObject,CIObject thumbnailObject){
        ImageInfo imageInfo = originalInfo.getImageInfo();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        //这里需要加协议 originalInfo.getLocation()返回的类似 xxxxx.cos.ap-guangzhou.myqcloud.com/avatar/1923671373394776065/2025-05-19_td9tqddfx0yhs4h6.webp
        uploadPictureResult.setOriginUrl("https://"+originalInfo.getLocation());
        uploadPictureResult.setOriginSize((long) imageInfo.getQuality());
        uploadPictureResult.setOriginFormat(imageInfo.getFormat());
        uploadPictureResult.setOriginWidth(imageInfo.getWidth());
        uploadPictureResult.setOriginHeight(imageInfo.getHeight());
        uploadPictureResult.setOriginColor(imageInfo.getAve());
        uploadPictureResult.setOriginScale(NumberUtil.round(((double) imageInfo.getWidth()/imageInfo.getHeight()),2).doubleValue());
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));

        //如果没有就存 原图的数据
        if (compressObject!=null){
            uploadPictureResult.setCompressUrl("https://"+compressObject.getLocation());
            uploadPictureResult.setCompressFormat(compressObject.getFormat());
        }else{
            uploadPictureResult.setCompressUrl("https://"+originalInfo.getLocation());
            uploadPictureResult.setCompressFormat(imageInfo.getFormat());
        }
        
        if (thumbnailObject!=null){
            uploadPictureResult.setThumbnailUrl("https://"+thumbnailObject.getLocation());
        }else {
            uploadPictureResult.setThumbnailUrl("https://"+originalInfo.getLocation());
        }
        return uploadPictureResult;
    }


    /**
     * 将图片文件 赋给file
     * @param inputSource
     * @param file
     * @throws Exception
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     *获取原图 名称(包括后缀名)
     * @param inputSource
     * @return
     */
    protected abstract String getOriginFileName(Object inputSource);

    /**
     * 校验图片
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);


    /**
     * 删除 临时文件
     * @param file
     */
    public static void deleteTempFile(File file) {
        if (file != null) {
            boolean result = file.delete();
            if (!result) {
                //获取一下这个绝对路径
                log.error("UploadPictureResult delete error, filepath = {}", file.getAbsolutePath());
            }
        }
    }
}

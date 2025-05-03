package com.katomegumi.zxpicturebackend.manager.upload;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;

import com.katomegumi.zxpicturebackend.core.config.CosClientConfig;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.manager.cos.CosManager;
import com.katomegumi.zxpicturebackend.model.dto.file.UploadPictureResult;

import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Deprecated 标记弃置 改用pictureUploadTemplate
 */
@Deprecated
@Service
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    public UploadPictureResult uploadPicture(MultipartFile multipartFile,String uploadPathPrefix) {
        //1.校验文件
        validPicture(multipartFile);
        //2.上传地址
        String uuid= RandomUtil.randomString(16);
        String originFilename = multipartFile.getOriginalFilename();
        String uploadFilename =String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;

        try {
            //3.上传到对象存储
            file=File.createTempFile(uploadPath,null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath,file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            String picFormat = imageInfo.getFormat();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            //4.封装返回类
            double picScale= NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(picFormat);

            return uploadPictureResult;

        } catch (Exception e) {
            log.error("图片上传到对象存储失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败了");
        } finally {
            //4.结束file对象
            deleteTempFile(file);
        }

    }

    public static void deleteTempFile(File file) {
        if (file != null) {
            boolean result = file.delete();
            if (!result) { //获取一下这个绝对路径
                log.error("UploadPictureResult delete error, filepath = {}", file.getAbsolutePath());
            }
        }
    }

    private void validPicture(MultipartFile multipartFile) {
        //1.校验是否为空
        ThrowUtils.throwIf(multipartFile==null, ErrorCode.PARAMS_ERROR,"文件不能为空");
        //2.文件大小 (1M)
        final long ONE_M=1024*1024L;
        ThrowUtils.throwIf(multipartFile.getSize()>2*ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过2M");
        //3.后缀是否符合
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix),ErrorCode.PARAMS_ERROR,"文件类型错误");
    }

    /**
     * 根据 URL下载图片
     * @param fileUrl
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl,String uploadPathPrefix) {
        //1.校验文件
        //validPicture(multipartFile);
        validPicture(fileUrl);
        //2.上传地址
        String uuid= RandomUtil.randomString(16);
        //String originFilename = multipartFile.getOriginalFilename();
        String originFilename=FileUtil.mainName(fileUrl);
        String uploadFilename =String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        //3.封装返回类
        File file = null;

        try {
            file=File.createTempFile(uploadPath,null);
            //multipartFile.transferTo(file);
            //通过http获取图片本地 再进行上传
            HttpUtil.downloadFile(fileUrl,file);

            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath,file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            String picFormat = imageInfo.getFormat();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();

            double picScale= NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(picFormat);

            return uploadPictureResult;

        } catch (Exception e) {
            log.error("图片上传到对象存储失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败了");
        } finally {
            //4.结束file对象
            deleteTempFile(file);
        }

    }

    /**
     * 校验图片地址
     * @param fileUrl
     */
    private void validPicture(String fileUrl){
        //判断是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl),ErrorCode.PARAMS_ERROR);
        //url格式是否正确
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"地址格式不正确");
        }
        //校验http协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://")||!fileUrl.startsWith("https://"),ErrorCode.PARAMS_ERROR,"仅支持 HTTP 或 HTTPS 协议的文件地址");
        //URL内容是否正确 是图片
        HttpResponse response=null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 校验文件类型
            String contentType = response.header("Content-Type");
            // 允许的图片类型
            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            //判断文件大小
            String contentLength = response.header("Content-Length");
            try {
                if (StrUtil.isNotBlank(contentLength)){
                    long contentLong = Long.parseLong(contentLength);
                    final long ONE_M=1024*1024L;
                    ThrowUtils.throwIf(contentLong>2*ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过2M");
                }
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
            }
        } finally {
            //一定记得释放资源
            if (response!=null) {
                response.close();
            }
        }
    }
}

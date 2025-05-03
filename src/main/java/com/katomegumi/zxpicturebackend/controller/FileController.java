package com.katomegumi.zxpicturebackend.controller;

import com.katomegumi.zxpicturebackend.core.annotation.AuthCheck;
import com.katomegumi.zxpicturebackend.core.common.resp.BaseResponse;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import com.katomegumi.zxpicturebackend.core.constant.UserConstant;

import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.manager.cos.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;
    @Autowired
    private View error;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PutMapping("/test/upload")
    public BaseResponse<String> testUpload(@RequestParam("file") MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String filePath =String.format("/test/%s",filename);
        File file = null;

        try {
            //上传文件
            file=File.createTempFile(filename,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath,file);
            //返回文件网址
            return ResultUtils.success(filePath);

        } catch (Exception e) {
            log.error("上传失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败了");
        } finally {
            if (file != null) {
                boolean result = file.delete();
                if (!result) {
                    log.error("UploadPictureResult delete error, filepath = {}", filePath);
                }
            }
        }
    }
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/download")
    public void testDownload(String filename, HttpServletResponse response) throws Exception {
        String filePath =String.format("/test/%s",filename);
        COSObjectInputStream cosObjectInputStream = null;

        try {
            //从COS获取图片
            COSObject cosManagerObject = cosManager.getObject(filePath);
            cosObjectInputStream=cosManagerObject.getObjectContent();
            //转成字节数组
            byte[] byteArray = IOUtils.toByteArray(cosObjectInputStream);
            //设置响应 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;filename="+filename);
            //写入响应
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("文件下载失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"下载失败");
        } finally {
            if (cosObjectInputStream != null) {
                cosObjectInputStream.close();
            }
        }

    }
}

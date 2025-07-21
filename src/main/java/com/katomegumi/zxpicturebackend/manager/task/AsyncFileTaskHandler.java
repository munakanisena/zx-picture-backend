package com.katomegumi.zxpicturebackend.manager.task;

import cn.hutool.core.util.StrUtil;
import com.katomegumi.zxpicturebackend.manager.cos.CosManager;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Megumi
 * @description 异步文件处理
 */
@Component
@RequiredArgsConstructor
public class AsyncFileTaskHandler {

    private final CosManager cosManager;

    /**
     * 清理文件
     *
     * @param pictureInfo 文件信息
     */
    @Async("cosThreadPool")
    public void clearPictureFile(PictureInfo pictureInfo) {

        String originPath = pictureInfo.getOriginPath();
        String thumbnailPath = pictureInfo.getThumbnailPath();
        String compressPath = pictureInfo.getCompressPath();

        if (StrUtil.isNotBlank(originPath)) {
            cosManager.deleteObject(originPath);
        }
        if (StrUtil.isNotBlank(thumbnailPath)) {
            cosManager.deleteObject(thumbnailPath);
        }
        if (StrUtil.isNotBlank(compressPath)) {
            cosManager.deleteObject(compressPath);
        }
    }

    /**
     * 批量清理文件
     *
     * @param keys 文件key列表
     */
    @Async("cosThreadPool")
    public void clearPictureFiles(List<String> keys) {
        cosManager.batchDeleteObjects(keys);
    }
}
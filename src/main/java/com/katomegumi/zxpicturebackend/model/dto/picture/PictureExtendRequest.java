package com.katomegumi.zxpicturebackend.model.dto.picture;

import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Megumi
 * @description 扩图请求(仅支持上传到图库的图片扩图)
 */
@Data
public class PictureExtendRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 图片 id
     */
    private Long pictureId;
    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;
}


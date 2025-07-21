package com.katomegumi.zxpicturebackend.model.dto.picture;

import com.katomegumi.zxpicturebackend.model.enums.SpaceTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 上传图片
 *
 * @author Megumi
 */
@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片 id（用于修改重复上传）
     */
    private Long id;

    /**
     * 文件地址
     */
    private String pictureUrl;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 图片名称
     */
    private String pictureName;
}

package com.katomegumi.zxpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Megumi
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 随机种子, 应该大于 0 小于 100
     */
    private Integer randomSeed = 1;

    /**
     * 搜索数量(默认10)
     */
    private Integer searchCount = 10;

}

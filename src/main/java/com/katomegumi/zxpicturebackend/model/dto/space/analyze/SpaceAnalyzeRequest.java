package com.katomegumi.zxpicturebackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间通用分析请求
 *
 * @author Megumi
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    private boolean queryPublic;

    /**
     * 全空间分析
     */
    private boolean queryAll;

}

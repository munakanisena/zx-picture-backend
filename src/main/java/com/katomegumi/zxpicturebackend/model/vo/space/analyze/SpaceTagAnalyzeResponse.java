package com.katomegumi.zxpicturebackend.model.vo.space.analyze;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SpaceTagAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签的图片数量
     */
    private Long count;
}

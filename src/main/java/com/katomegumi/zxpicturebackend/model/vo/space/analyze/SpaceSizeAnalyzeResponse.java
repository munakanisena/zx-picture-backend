package com.katomegumi.zxpicturebackend.model.vo.space.analyze;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SpaceSizeAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片大小范围
     */
    private String sizeRange;

    /**
     * 图片数量
     */
    private Long count;
}

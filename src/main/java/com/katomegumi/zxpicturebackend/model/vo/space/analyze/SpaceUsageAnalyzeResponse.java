package com.katomegumi.zxpicturebackend.model.vo.space.analyze;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Megumi
 */
@Data
@Builder
public class SpaceUsageAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总大小
     */
    private Long maxSize;

    /**
     * 已使用大小
     */
    private Long usedSize;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 当前图片数量
     */
    private Long usedCount;

    /**
     * 图片数量占比
     */
    private Double countUsageRatio;

    /**
     * 空间使用比例
     */
    private Double sizeUsageRatio;

}

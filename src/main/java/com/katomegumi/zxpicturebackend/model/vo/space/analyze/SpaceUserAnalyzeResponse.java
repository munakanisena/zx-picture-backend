package com.katomegumi.zxpicturebackend.model.vo.space.analyze;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SpaceUserAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 时间区间
     */
    private String period;
    /**
     * 上传数量
     */
    private Long count;
}

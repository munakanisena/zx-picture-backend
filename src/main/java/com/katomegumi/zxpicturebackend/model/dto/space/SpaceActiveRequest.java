package com.katomegumi.zxpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceActiveRequest implements Serializable {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别: 0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间类型: 1-私有 2-团队
     */
    private Integer spaceType;

    private static final long serialVersionUID = 1L;
}

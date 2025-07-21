package com.katomegumi.zxpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : Megumi
 * @description : 空间切换请求
 * @createDate : 2025/7/20 下午1:31
 */
@Data
public class SpaceSwitchRequest implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 空间id
     */
    private Long spaceId;
}


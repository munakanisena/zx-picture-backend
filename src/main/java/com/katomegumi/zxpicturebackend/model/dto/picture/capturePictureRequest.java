package com.katomegumi.zxpicturebackend.model.dto.picture;

import cn.hutool.db.handler.StringHandler;
import lombok.Data;

import java.io.Serializable;

/**
 *  批量获取图片
 * @author Megumi
 */
@Data
public class capturePictureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 爬取源
     */
    private String captureSource;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 获取数量 (默认10张)
     */
    private Integer captureCount = 10;

    /**
     * 图片前缀
     */
    private String namePrefix;

    /**
     * 随机种子(其实就是爬取 第几张图片) 1-100
     */
    private Integer randomSeed;
}

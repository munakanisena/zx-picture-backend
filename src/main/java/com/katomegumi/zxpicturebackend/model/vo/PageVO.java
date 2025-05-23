package com.katomegumi.zxpicturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author : Megumi
 * @description :自定义page
 * @createDate : 2025/5/10 下午2:19
 */
@Data
public class PageVO <T> implements Serializable {

    /**
     * 当前页
     */
    private long current = 1;

    /**
     * 每页显示条数，默认 10
     */
    private long pageSize = 10;

    /**
     * 总数
     */
    private long total = 0;

    /**
     * 总页数
     */
    private long pages = 0;

    /**
     * 查询数据列表
     */
    private List<T> records = Collections.emptyList();

    public PageVO() {
    }

    public PageVO(long current, long pageSize, long total, long pages, List<T> records) {
        this.current = current;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pages;
        this.records = records;
    }
}


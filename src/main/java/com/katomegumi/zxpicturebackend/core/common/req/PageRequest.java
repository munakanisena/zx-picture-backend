package com.katomegumi.zxpicturebackend.core.common.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求包装类 对于 “分页”、“删除某条数据” 这类通用的请求，可以封装统一的请求包装类，
 *
 * @author Megumi
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页号 默认为1
     */
    private int current = 1;

    /**
     * 页面大小 默认为10
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）false 降序
     */
    private Boolean sortOrder = false;

    /**
     * 获取分页对象
     *
     * @param clazz 类对象
     * @param <T>   泛型
     * @return 分页对象(mybatis - plus)
     */
    public <T> Page<T> getPage(Class<T> clazz) {
        return new Page<T>(this.current, this.pageSize);
    }
}

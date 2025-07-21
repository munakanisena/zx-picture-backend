package com.katomegumi.zxpicturebackend.model.dto.picture;

import com.katomegumi.zxpicturebackend.core.common.req.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 相似图片搜索请求体
 *
 * @author Megumi
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchPictureByColorRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主色调
     */
    private String picColor;
    /**
     * 空间id
     */
    private Long spaceId;
}

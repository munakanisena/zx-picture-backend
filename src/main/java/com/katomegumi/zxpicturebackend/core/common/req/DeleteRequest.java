package com.katomegumi.zxpicturebackend.core.common.req;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Megumi
 */
@Data
public class DeleteRequest implements Serializable {

    private static  final long serialVersionUID = 1L;

    /**
     * 要删除的id
     */
    private Long id;

}

package com.katomegumi.zxpicturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUserEditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 空间用户(表id)
     */
    private Long id;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 空间角色: viewer/editor/admin
     */
    private String spaceRole;
}

package com.katomegumi.zxpicturebackend.model.vo.space.user;

import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Megumi
 * @description 团队空间用户信息
 */
@Data
public class SpaceUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户信息
     */
    private UserDetailVO userDetailVO;

    /**
     * 空间角色: viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

}

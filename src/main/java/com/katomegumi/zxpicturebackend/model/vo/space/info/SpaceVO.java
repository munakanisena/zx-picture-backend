package com.katomegumi.zxpicturebackend.model.vo.space.info;

import cn.hutool.core.bean.BeanUtil;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceInfo;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class SpaceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别: 0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间类型:  1-私有 2-团队
     */
    private Integer spaceType;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间下图片的总大小(单位:KB)
     */
    private Long usedSize;

    /**
     * 当前空间下的图片数量
     */
    private Long usedCount;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 权限列表
     */
    private List<String> permissionList = new ArrayList<>();

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserDetailVO user;

    /**
     * 封装类转对象
     *
     * @param spaceVO 封装类
     * @return 对象
     */
    public static SpaceInfo voToObj(SpaceVO spaceVO) {
        if (spaceVO == null) {
            return null;
        }
        return BeanUtil.copyProperties(spaceVO, SpaceInfo.class);
    }

    /**
     * 对象转封装类
     *
     * @param spaceInfo 对象
     * @return 封装类
     */
    public static SpaceVO objToVo(SpaceInfo spaceInfo) {
        if (spaceInfo == null) {
            return null;
        }
        return BeanUtil.copyProperties(spaceInfo, SpaceVO.class);
    }
}


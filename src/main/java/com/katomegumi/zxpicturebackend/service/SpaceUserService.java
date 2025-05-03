package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lirui
* @description 针对表【tb_space_user(团队用户关联)】的数据库操作Service
* @createDate 2025-03-12 17:58:55
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 构造查询对象
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 查询单个封装类
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 查询封装类列表
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 校验权限
     * @param spaceUser
     * @param add
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);
}

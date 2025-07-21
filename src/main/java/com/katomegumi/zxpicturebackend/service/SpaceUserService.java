package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceUser;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.katomegumi.zxpicturebackend.model.vo.space.user.SpaceUserVO;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【space_user(团队用户关联)】的数据库操作Service
 * @createDate 2025-06-10 20:09:10
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户成员
     *
     * @param spaceUserAddRequest 成员添加请求
     */
    void addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 移除空间成员
     *
     * @param spaceUserId 空间用户id
     */
    void deleteSpaceUser(Long spaceUserId);

    /**
     * 编辑空间成员
     *
     * @param spaceUserEditRequest 编辑请求
     */
    void editSpaceUser(SpaceUserEditRequest spaceUserEditRequest);

    /**
     * 根据空间ID获取团队空间成员列表
     *
     * @param spaceId 团队空间id
     * @return 成员列表
     */
    List<SpaceUserVO> getTeamSpaceMembersBySpaceId(Long spaceId);

    /**
     * 根据用户id 用户空间角色列表
     *
     * @param userId 用户id
     * @return 用户团队空间角色列表
     */
    List<SpaceUser> getSpaceUserListByUserId(Long userId);

    /**
     * 查询用户各个团队空间权限
     *
     * @return 空间权限列表
     */
    List<SpaceUserVO> queryUserTeamSpacePermissions(SpaceUserQueryRequest spaceUserQueryRequest);


    /**
     * 校验空间用户 实体
     *
     * @param spaceUser 用户角色
     * @param isAdd 是否新增
     */
    void validSpaceUser(SpaceUser spaceUser, Boolean isAdd);


}

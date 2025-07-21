package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceActiveRequest;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceEditRequest;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceQueryRequest;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceTeamDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceVO;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【space_info(空间信息表)】的数据库操作Service
 * @createDate 2025-06-10 20:09:10
 */
public interface SpaceService extends IService<SpaceInfo> {

    /**
     * 激活空间
     *
     * @param spaceActiveRequest 激活请求
     */
    void activeSpace(SpaceActiveRequest spaceActiveRequest);

    /**
     * 编辑用户
     *
     * @param spaceEditRequest 编辑用户
     */
    void editSpace(SpaceEditRequest spaceEditRequest);

    /**
     * 校验空间权限
     *
     * @param spaceInfo 空间信息
     * @param userInfo  用户信息
     */
    void checkSpaceAuth(SpaceInfo spaceInfo, UserInfo userInfo);

    /**
     * 根据空间id校验空间是否存在
     *
     * @param spaceId 空间id
     */
    void existedSpaceBySpaceId(Long spaceId);

    /**
     * 根据空间等级填充空间信息
     *
     * @param spaceInfo 空间信息
     */
    void fillSpaceInfoBySpaceLevel(SpaceInfo spaceInfo);

    /**
     * 校验空间信息
     *
     * @param spaceInfo 空间信息
     */
    void checkSpaceInfo(SpaceInfo spaceInfo);


    /**
     * 更新用户(管理员)
     *
     * @param spaceUpdateRequest 更新请求
     */
    void updateSpace(SpaceUpdateRequest spaceUpdateRequest);

    /**
     * 删除空间
     *
     * @param spaceId
     */
    void deleteSpace(Long spaceId);

    /**
     * 登录用户获取私人空间详情
     *
     * @return 私人空间详情
     */
    SpaceDetailVO getSpaceDetailByLoginUser();


    /**
     * 登录用户获取团队空间详情(自己)
     *
     * @return 团队空间详情
     */
    SpaceTeamDetailVO getTeamSpaceDetailByLoginUser();


    /**
     * 获取登录用户加入的团队空间
     *
     * @return 团队空间列表
     */
    List<SpaceTeamDetailVO> getJoinTeamSpacesByLoginUser();


    /**
     * 根据空间ID获取空间详情
     *
     * @return 空间详情
     */
    SpaceDetailVO getSpaceDetailBySpaceId(Long spaceId);


    /**
     * 切换当前操作的空间上下文
     *
     * @param spaceId 切换id
     */
    void switchSpaceContext(Long spaceId);

    /**
     * 获取空间列表(管理员)
     *
     * @param spaceQueryRequest 查询请求
     * @return 空间列表
     */
    PageVO<SpaceVO> getSpacePageListAsManage(SpaceQueryRequest spaceQueryRequest);

    /**
     * 更新空间额度
     *
     * @param pictureInfo 图片信息
     * @param isAdd       是否增加 true表示增加 false表示减少
     */
    void updateSpaceAmount(PictureInfo pictureInfo, boolean isAdd);

    /**
     * 检查空间额度
     *
     * @param spaceInfo 空间信息
     */
    void checkSpaceAmount(SpaceInfo spaceInfo);

}

package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceUser;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.dao.mapper.SpaceInfoMapper;
import com.katomegumi.zxpicturebackend.model.dao.mapper.SpaceUserMapper;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.katomegumi.zxpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.katomegumi.zxpicturebackend.model.enums.SpaceTypeEnum;
import com.katomegumi.zxpicturebackend.model.vo.space.user.SpaceUserVO;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import com.katomegumi.zxpicturebackend.service.SpaceUserService;
import com.katomegumi.zxpicturebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【space_user(团队用户关联)】的数据库操作Service实现
 * @createDate 2025-06-10 20:09:10
 */
@Service
@RequiredArgsConstructor
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    private final UserService userService;

    private final SpaceInfoMapper spaceInfoMapper;

    @Override
    public void addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true);
        //用户是否存在?
        boolean exists = lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                .eq(SpaceUser::getUserId, spaceUser.getUserId())
                .exists();
        if (!exists) {
            boolean result = this.save(spaceUser);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            return;
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已存在");
    }


    @Override
    public void deleteSpaceUser(Long spaceUserId) {
        boolean exists = lambdaQuery().eq(SpaceUser::getId, spaceUserId).exists();
        if (exists) {
            boolean remove = this.removeById(spaceUserId);
            ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR);
            return;
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户不存在");
    }

    @Override
    public void editSpaceUser(SpaceUserEditRequest spaceUserEditRequest) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
        validSpaceUser(spaceUser, false);
        boolean exists = lambdaQuery().eq(SpaceUser::getId, spaceUser.getId()).exists();
        if (exists) {
            boolean result = this.updateById(spaceUser);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            return;
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户不存在");
    }

    @Override
    public List<SpaceUserVO> getTeamSpaceMembersBySpaceId(Long spaceId) {
        SpaceInfo spaceInfo = spaceInfoMapper.selectById(spaceId);
        ThrowUtils.throwIf(spaceInfo == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!SpaceTypeEnum.TEAM.getKey().equals(spaceInfo.getSpaceType()), ErrorCode.OPERATION_ERROR, "当前空间不是团队空间");
        long userId = StpKit.USER.getLoginIdAsLong();
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setSpaceId(spaceId);
        spaceUserQueryRequest.setUserId(userId);
        LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper = this.getLambdaQueryWrapper(spaceUserQueryRequest);
        boolean exists = this.exists(lambdaQueryWrapper);
        //说明是团队成员
        if (exists) {
            //获取团队成员列表
            List<SpaceUser> spaceUserList = this.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId).list();
            List<Long> useIds = spaceUserList.stream()
                    .map(SpaceUser::getUserId)
                    .collect(Collectors.toList());
            List<UserInfo> userInfoList = userService.listByIds(useIds);
            Map<Long, UserDetailVO> userDetailVOMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, userInfo -> BeanUtil.copyProperties(userInfo, UserDetailVO.class)));
            //填充团队成员用户信息
            return spaceUserList
                    .stream()
                    .map(spaceUser -> {
                        SpaceUserVO spaceUserVO = BeanUtil.copyProperties(spaceUser, SpaceUserVO.class);
                        spaceUserVO.setUserDetailVO(userDetailVOMap.get(spaceUser.getUserId()));
                        return spaceUserVO;
                    }).collect(Collectors.toList());
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "当前用户不是团队空间的成员");
    }

    @Override
    public List<SpaceUserVO> queryUserTeamSpacePermissions(SpaceUserQueryRequest spaceUserQueryRequest) {
        LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper = this.getLambdaQueryWrapper(spaceUserQueryRequest);
        return Optional.ofNullable(this.list(lambdaQueryWrapper))
                .orElse(Collections.emptyList())
                .stream()
                .map(spaceUser -> BeanUtil.copyProperties(spaceUser, SpaceUserVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void validSpaceUser(SpaceUser spaceUser, Boolean isAdd) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        if (isAdd) {
            Long userId = spaceUser.getUserId();
            Long spaceId = spaceUser.getSpaceId();
            UserInfo userInfo = userService.getById(userId);
            ThrowUtils.throwIf(userInfo == null, ErrorCode.NOT_FOUND_ERROR, "添加的用户不存在");
            SpaceInfo spaceInfo = spaceInfoMapper.selectById(spaceId);
            ThrowUtils.throwIf(spaceInfo == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
    }

    @Override
    public List<SpaceUser> getSpaceUserListByUserId(Long userId) {
        return Optional
                .ofNullable(this.list(new LambdaQueryWrapper<SpaceUser>().eq(SpaceUser::getUserId, userId)))
                .orElse(Collections.emptyList());
    }

    private LambdaQueryWrapper<SpaceUser> getLambdaQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();

        LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ObjectUtil.isNotNull(spaceId), SpaceUser::getSpaceId, spaceId)
                .eq(ObjectUtil.isNotNull(userId), SpaceUser::getUserId, userId)
                .eq(StrUtil.isNotBlank(spaceRole), SpaceUser::getSpaceRole, spaceRole);

        return lambdaQueryWrapper;
    }


}



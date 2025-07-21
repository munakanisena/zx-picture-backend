package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.util.SFunctionUtils;
import com.katomegumi.zxpicturebackend.manager.auth.SpaceUserAuthManager;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.manager.task.AsyncFileTaskHandler;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceUser;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureInfoMapper;
import com.katomegumi.zxpicturebackend.model.dao.mapper.SpaceInfoMapper;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceActiveRequest;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceEditRequest;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceQueryRequest;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.katomegumi.zxpicturebackend.model.enums.SpaceLevelEnum;
import com.katomegumi.zxpicturebackend.model.enums.SpaceRoleEnum;
import com.katomegumi.zxpicturebackend.model.enums.SpaceTypeEnum;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceTeamDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceVO;
import com.katomegumi.zxpicturebackend.service.SpaceService;
import com.katomegumi.zxpicturebackend.service.SpaceUserService;
import com.katomegumi.zxpicturebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lirui
 * @description 针对表【space_info(空间信息表)】的数据库操作Service实现
 * @createDate 2025-06-10 20:09:10
 */
@Service
@RequiredArgsConstructor
public class SpaceServiceImpl extends ServiceImpl<SpaceInfoMapper, SpaceInfo>
        implements SpaceService {

    private final UserService userService;

    private final SpaceUserService spaceUserService;

    private final PictureInfoMapper pictureInfoMapper;

    private final TransactionTemplate transactionTemplate;

    private final AsyncFileTaskHandler asyncFileTaskHandler;

    private final SpaceUserAuthManager spaceUserAuthManager;

    @Override
    public void activeSpace(SpaceActiveRequest spaceActiveRequest) {
        //1.转换space
        SpaceInfo spaceInfo = BeanUtil.copyProperties(spaceActiveRequest, SpaceInfo.class);
        //2.填充默认参数
        if (StrUtil.isBlank(spaceInfo.getSpaceName())) {
            spaceInfo.setSpaceName("默认空间" + RandomUtil.randomString(5));
        }
        if (spaceInfo.getSpaceLevel() == null) {
            spaceInfo.setSpaceLevel(SpaceLevelEnum.COMMON.getKey());
        }
        if (spaceInfo.getSpaceType() == null) {
            spaceInfo.setSpaceType(SpaceTypeEnum.PRIVATE.getKey());
        }
        //1.填充空间参数
        this.fillSpaceInfoBySpaceLevel(spaceInfo);
        //2.校验空间信息
        this.checkSpaceInfo(spaceInfo);
        UserInfo userInfo = userService.getCurrentUserInfo();
        Long userId = userInfo.getId();
        spaceInfo.setUserId(userId);
        //3.检验权限 判断是否为管理员(只有管理员才能创建除普通空间以外的)
        if (SpaceLevelEnum.COMMON.getKey() != spaceActiveRequest.getSpaceLevel() && !userService.isAdmin(userInfo)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            transactionTemplate.execute(status -> {
                boolean exists = this.lambdaQuery()
                        .eq(SpaceInfo::getUserId, userId)
                        .eq(SpaceInfo::getSpaceType, spaceInfo.getSpaceType())
                        .exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能有一个私有空间或者团队空间");
                boolean result = save(spaceInfo);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存失败，请重试");
                //在创建空间的时候 如果是创建的团队空间 自动创建空间用户表(设置为管理员)
                if (spaceInfo.getSpaceType().equals(SpaceTypeEnum.TEAM.getKey())) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceId(spaceInfo.getId());
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getKey());
                    boolean save = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                }
                //添加分表
                //dynamicShardingManager.createTable(space); 关闭分表
                return true;
            });
        }
    }

    @Override
    public void editSpace(SpaceEditRequest spaceEditRequest) {
        Long spaceId = spaceEditRequest.getId();
        this.existedSpaceBySpaceId(spaceId);
        long userId = StpKit.USER.getLoginIdAsLong();
        SpaceInfo oldSpaceInfo = this.getById(spaceId);
        //校验权限
        UserInfo userInfo = userService.getCurrentUserInfo();
        this.checkSpaceAuth(oldSpaceInfo, userInfo);

        BeanUtil.copyProperties(spaceEditRequest, oldSpaceInfo);
        boolean result = this.updateById(oldSpaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "修改失败");
    }

    @Override
    public void updateSpace(SpaceUpdateRequest spaceUpdateRequest) {
        SpaceInfo spaceInfo = BeanUtil.copyProperties(spaceUpdateRequest, SpaceInfo.class);
        this.existedSpaceBySpaceId(spaceUpdateRequest.getId());
        this.fillSpaceInfoBySpaceLevel(spaceInfo);
        boolean result = this.updateById(spaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "修改失败");
    }

    @Override
    public void deleteSpace(Long spaceId) {
        //1.效验
        this.existedSpaceBySpaceId(spaceId);
        UserInfo userInfo = userService.getCurrentUserInfo();
        SpaceInfo spaceInfo = this.getById(spaceId);
        this.checkSpaceAuth(spaceInfo, userInfo);

        //2.删除空间和图片
        List<String> keys = transactionTemplate.execute(status -> {

            LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .select(PictureInfo::getOriginPath, PictureInfo::getCompressPath, PictureInfo::getThumbnailPath)
                    .eq(PictureInfo::getSpaceId, spaceId);

            List<PictureInfo> pictureInfoList = pictureInfoMapper.selectList(lambdaQueryWrapper);

            List<String> keyList = pictureInfoList.stream()
                    .flatMap(pictureInfo -> Stream.of(
                            pictureInfo.getOriginPath(),
                            pictureInfo.getCompressPath(),
                            pictureInfo.getThumbnailPath()
                    ))
                    .filter(ObjectUtil::isNotNull)
                    .collect(Collectors.toList());

            // 2.2 删除图片记录
            int deleteCount = pictureInfoMapper.delete(lambdaQueryWrapper);
            ThrowUtils.throwIf(deleteCount != pictureInfoList.size(),
                    ErrorCode.OPERATION_ERROR, "部分图片删除失败");

            // 2.3 删除空间记录
            boolean spaceDeleted = this.removeById(spaceId);
            ThrowUtils.throwIf(!spaceDeleted, ErrorCode.OPERATION_ERROR, "空间删除失败");

            return keyList;
        });

        if (keys == null) {
            return;
        }

        //3.清除cos资源
        if (CollUtil.isNotEmpty(keys)) {
            asyncFileTaskHandler.clearPictureFiles(keys);
        }
    }

    @Override
    public SpaceDetailVO getSpaceDetailByLoginUser() {
        long userId = StpKit.USER.getLoginIdAsLong();
        SpaceInfo spaceInfo = this.lambdaQuery()
                .eq(SpaceInfo::getUserId, userId)
                .eq(SpaceInfo::getSpaceType, SpaceTypeEnum.PRIVATE.getKey()).one();
        ThrowUtils.throwIf(spaceInfo == null, ErrorCode.NOT_FOUND_ERROR, "私人空间不存在");
        return BeanUtil.copyProperties(spaceInfo, SpaceDetailVO.class);
    }

    @Override
    public SpaceTeamDetailVO getTeamSpaceDetailByLoginUser() {
        long userId = StpKit.USER.getLoginIdAsLong();
        SpaceInfo spaceInfo = this.lambdaQuery()
                .eq(SpaceInfo::getUserId, userId)
                .eq(SpaceInfo::getSpaceType, SpaceTypeEnum.TEAM.getKey()).one();
        ThrowUtils.throwIf(spaceInfo == null, ErrorCode.NOT_FOUND_ERROR, "团队空间不存在");
        return BeanUtil.toBean(spaceInfo, SpaceTeamDetailVO.class);
    }

    @Override
    public List<SpaceTeamDetailVO> getJoinTeamSpacesByLoginUser() {
        long userId = StpKit.USER.getLoginIdAsLong();
        List<SpaceUser> spaceUserList = spaceUserService.getSpaceUserListByUserId(userId);
        if (CollectionUtil.isEmpty(spaceUserList)) {
            return List.of();
        }
        Set<Long> spaceId = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        return Optional
                .ofNullable(this.listByIds(spaceId)).orElse(Collections.emptyList())
                .stream().map(spaceInfo -> BeanUtil.copyProperties(spaceInfo, SpaceTeamDetailVO.class)).collect(Collectors.toList());
    }

    @Override
    public SpaceDetailVO getSpaceDetailBySpaceId(Long spaceId) {
        this.existedSpaceBySpaceId(spaceId);
        SpaceInfo spaceInfo = this.getById(spaceId);
        UserInfo userInfo = userService.getCurrentUserInfo();
        this.checkSpaceAuth(spaceInfo, userInfo);
        return BeanUtil.copyProperties(spaceInfo, SpaceDetailVO.class);
    }

    @Override
    public void switchSpaceContext(Long spaceId) {
        SpaceInfo spaceInfo = this.getById(spaceId);
        ThrowUtils.throwIf(spaceInfo == null, ErrorCode.PARAMS_ERROR, "空间不存在");
        UserInfo userInfo = userService.getCurrentUserInfo();
        //这里校验一下是否有权限
        this.checkSpaceAuth(spaceInfo, userInfo);
        Long userId = userInfo.getId();
        StpKit.SPACE.login(userId);

        //如果是私有空间 返回全部权限即可
        if (spaceInfo.getSpaceType().equals(SpaceTypeEnum.PRIVATE.getKey())) {
            //全部权限
            List<String> allPermissions = spaceUserAuthManager.getSpaceUserPermissionsByRole(SpaceRoleEnum.ADMIN.getKey());
            StpKit.SPACE.getSession()
                    .set("permissions", allPermissions)
                    .set("spaceId", spaceId);
        } else {
            //否则团队空间,获取空间角色对应的权限
            SpaceUser spaceUser = spaceUserService.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId).eq(SpaceUser::getUserId, userId).one();
            List<String> spaceUserPermissionsByRole = spaceUserAuthManager.getSpaceUserPermissionsByRole(spaceUser.getSpaceRole());
            StpKit.SPACE.getSession()
                    .set("permissions", spaceUserPermissionsByRole)
                    .set("spaceId", spaceId);
        }
    }

    @Override
    public PageVO<SpaceVO> getSpacePageListAsManage(SpaceQueryRequest spaceQueryRequest) {
        LambdaQueryWrapper<SpaceInfo> lambdaQueryWrapper = this.getLambdaQueryWrapper(spaceQueryRequest);
        Page<SpaceInfo> page = this.page(spaceQueryRequest.getPage(SpaceInfo.class), lambdaQueryWrapper);
        return new PageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                Optional.ofNullable(page.getRecords())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(spaceInfo -> BeanUtil.copyProperties(spaceInfo, SpaceVO.class))
                        .collect(Collectors.toList())

        );
    }

    @Override
    public void checkSpaceAuth(SpaceInfo spaceInfo, UserInfo userInfo) {
        if (spaceInfo.getUserId().equals(userInfo.getId()) || userService.isAdmin(userInfo)) {
            return;
        }
        //如果是团队空间
        if (SpaceTypeEnum.TEAM.getKey().equals(spaceInfo.getSpaceType())) {
            //检查是否是空间成员
            boolean exists = spaceUserService.exists(new LambdaQueryWrapper<SpaceUser>()
                    .eq(SpaceUser::getSpaceId, spaceInfo.getId())
                    .eq(SpaceUser::getUserId, userInfo.getId()));
            if (exists) {
                return;
            }
        }

        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    @Override
    public void existedSpaceBySpaceId(Long spaceId) {
        boolean result = this.exists(new LambdaQueryWrapper<SpaceInfo>()
                .eq(SpaceInfo::getId, spaceId));
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR, "空间不存在");
    }

    @Override
    public void fillSpaceInfoBySpaceLevel(SpaceInfo spaceInfo) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByKey(spaceInfo.getSpaceLevel());
        ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAMS_ERROR, "空间等级不存在");
        if (spaceInfo.getMaxSize() == null) {
            spaceInfo.setMaxSize(spaceLevelEnum.getMaxSize());
        }
        if (spaceInfo.getMaxCount() == null) {
            spaceInfo.setMaxCount(spaceLevelEnum.getMaxCount());
        }
    }

    @Override
    public void checkSpaceInfo(SpaceInfo spaceInfo) {
        ThrowUtils.throwIf(StrUtil.isBlank(spaceInfo.getSpaceName()), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
        ThrowUtils.throwIf(spaceInfo.getSpaceName().length() > 30, ErrorCode.PARAMS_ERROR, "空间名称不能超过30个字符");
        ThrowUtils.throwIf(spaceInfo.getSpaceLevel() == null, ErrorCode.PARAMS_ERROR, "空间等级不能为空");
        ThrowUtils.throwIf(spaceInfo.getSpaceType() == null, ErrorCode.PARAMS_ERROR, "空间类型不能为空");
        ThrowUtils.throwIf(spaceInfo.getMaxCount() == null, ErrorCode.PARAMS_ERROR, "最大图片数量不能为空");
        ThrowUtils.throwIf(spaceInfo.getMaxSize() == null, ErrorCode.PARAMS_ERROR, "最大图片大小不能为空");
    }

    @Override
    public void updateSpaceAmount(PictureInfo pictureInfo, boolean isAdd) {
        boolean result;
        if (isAdd) {
            result = this.lambdaUpdate()
                    .eq(SpaceInfo::getId, pictureInfo.getSpaceId())
                    .setIncrBy(SpaceInfo::getUsedSize, pictureInfo.getOriginSize())
                    .setIncrBy(SpaceInfo::getUsedCount, 1)
                    .update();
        } else {
            result = this.lambdaUpdate()
                    .eq(SpaceInfo::getId, pictureInfo.getSpaceId())
                    .setIncrBy(SpaceInfo::getUsedSize, -pictureInfo.getOriginSize())
                    .setIncrBy(SpaceInfo::getUsedCount, -1)
                    .update();
        }
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新图库空间失败");
    }

    @Override
    public void checkSpaceAmount(SpaceInfo spaceInfo) {
        if (spaceInfo.getUsedCount() >= spaceInfo.getMaxCount()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
        }
        if (spaceInfo.getUsedSize() >= spaceInfo.getMaxSize()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
        }
    }

    /**
     * 构造请求参数
     *
     * @param spaceQueryRequest 请求参数
     * @return lambdaQueryWrapper
     */
    private LambdaQueryWrapper<SpaceInfo> getLambdaQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        LambdaQueryWrapper<SpaceInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Long spaceId = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        Boolean sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        //拼接查询条件
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(spaceId), SpaceInfo::getId, spaceId);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(userId), SpaceInfo::getUserId, userId);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(spaceName), SpaceInfo::getSpaceName, spaceName);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(spaceLevel), SpaceInfo::getSpaceLevel, spaceLevel);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(spaceType), SpaceInfo::getSpaceType, spaceType);

        //构造排序
        if (sortField != null) {
            lambdaQueryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder, SFunctionUtils.getSFunction(SpaceInfo.class, sortField));
        } else {
            lambdaQueryWrapper.orderByDesc(SpaceInfo::getCreateTime);
        }
        return lambdaQueryWrapper;
    }
}





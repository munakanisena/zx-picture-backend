package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.exception.BusinessException;
import com.katomegumi.zxpicturebackend.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.manager.sharding.DynamicShardingManager;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceAddRequest;
import com.katomegumi.zxpicturebackend.model.dto.space.SpaceQueryRequest;
import com.katomegumi.zxpicturebackend.model.entity.Space;
import com.katomegumi.zxpicturebackend.model.entity.SpaceUser;
import com.katomegumi.zxpicturebackend.model.entity.User;
import com.katomegumi.zxpicturebackend.model.enums.SpaceLevelEnum;

import com.katomegumi.zxpicturebackend.mapper.SpaceMapper;
import com.katomegumi.zxpicturebackend.model.enums.SpaceRoleEnum;
import com.katomegumi.zxpicturebackend.model.enums.SpaceTypeEnum;
import com.katomegumi.zxpicturebackend.model.vo.SpaceVO;
import com.katomegumi.zxpicturebackend.model.vo.UserVO;
import com.katomegumi.zxpicturebackend.service.SpaceService;
import com.katomegumi.zxpicturebackend.service.SpaceUserService;
import com.katomegumi.zxpicturebackend.service.UserService;
import org.checkerframework.checker.formatter.qual.ReturnsFormat;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author lirui
* @description 针对表【tb_space(空间)】的数据库操作Service实现
* @createDate 2025-03-04 18:25:23
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate; //自定义事务

    @Resource
    private SpaceUserService spaceUserService;

//    @Resource
//    @Lazy
//    private DynamicShardingManager dynamicShardingManager;

    /**
     * 添加 用户添加空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser){
        //1.转换space
        ThrowUtils.throwIf(spaceAddRequest==null,ErrorCode.PARAMS_ERROR);
        Space space = BeanUtil.copyProperties(spaceAddRequest, Space.class);
        if (StrUtil.isBlank(space.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null){
            //设置
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //设置默认值
        if (space.getSpaceType()==null){
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        //1.填充参数
        fillSpaceBySpaceLevel(space);
        //2.校验参数
        validSpace(space,true);
        Long userId = loginUser.getId();
        //补充参数
        space.setUserId(userId);
        //3.检验权限 判断是否为管理员
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        //针对用户加锁 避免多线程情况下 用户创建多个空间
        //TODO 后续考虑使用redis 的redisson完成
        String lock=String.valueOf(userId).intern();
        synchronized (lock){
            Long newSpaceId = transactionTemplate.execute(status -> {
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType,space.getSpaceType())
                        .exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能有一个私有空间或者团队空间");
                boolean result = save(space);
                ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR,"保存到数据库失败，请重试");
                //在创建空间的时候 如果是创建的团队空间 自动创建空间用户表(设置为管理员)
                if (spaceAddRequest.getSpaceType()==SpaceTypeEnum.TEAM.getValue()){
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    boolean save = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!save,ErrorCode.OPERATION_ERROR,"创建团队成员记录失败");
                }
                //添加分表
                //dynamicShardingManager.createTable(space); 关闭分表
                //4.添加数据库
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    @Override
    public void validSpace(Space space,boolean add){
        ThrowUtils.throwIf(space==null, ErrorCode.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getSpaceLevelEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        if(add){
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR,"空间名称不为为空");
            ThrowUtils.throwIf(spaceLevelEnum==null, ErrorCode.PARAMS_ERROR,"空间等级不能为空");
            ThrowUtils.throwIf(spaceType==null, ErrorCode.PARAMS_ERROR,"空间类型能为空");
        }
        //修改数据
        ThrowUtils.throwIf(spaceLevel!=null&&spaceLevelEnum==null, ErrorCode.PARAMS_ERROR,"空间等级不存在");
        ThrowUtils.throwIf(StrUtil.isNotBlank(spaceName)&&spaceName.length()>30,ErrorCode.PARAMS_ERROR,"名称过长");
        ThrowUtils.throwIf(spaceTypeEnum==null&&spaceType!=null,ErrorCode.PARAMS_ERROR,"空间类别不存在");
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getSpaceLevelEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        //拼接查询条件
        queryWrapper.eq(ObjectUtil.isNotEmpty(id),"id",id);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userId),"userId",userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName),"spaceName",spaceName);
        queryWrapper.eq(ObjectUtil.isNotEmpty(spaceLevel),"spaceLevel",spaceLevel);
        queryWrapper.eq(ObjectUtil.isNotEmpty(spaceType),"spaceType",spaceType);

        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;

    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> records = spacePage.getRecords();
        //设定
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(records)) {
            return spaceVOPage;
        }

        List<SpaceVO> spaceVOList = records.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        //为了获得用户的详细信息
        Set<Long> userIds = records.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIds).stream().collect(Collectors.groupingBy(User::getId));

        for (SpaceVO spaceVO : spaceVOList) {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        }
        return spaceVOPage.setRecords(spaceVOList);
    }

    @Override
    public void checkSpaceAuth(Space space, User loginUser) {
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}





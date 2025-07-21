package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.BaiLianApi;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.core.api.capture.CapturePictureManager;
import com.katomegumi.zxpicturebackend.core.api.search.ImageSearchApiFacade;
import com.katomegumi.zxpicturebackend.core.api.search.model.SearchPictureResult;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.core.constant.CacheConstant;
import com.katomegumi.zxpicturebackend.core.constant.PictureConstant;
import com.katomegumi.zxpicturebackend.core.util.ColorSimilarUtils;
import com.katomegumi.zxpicturebackend.core.util.SFunctionUtils;
import com.katomegumi.zxpicturebackend.manager.auth.StpKit.StpKit;
import com.katomegumi.zxpicturebackend.manager.task.AsyncFileTaskHandler;
import com.katomegumi.zxpicturebackend.manager.upload.PictureFileUpload;
import com.katomegumi.zxpicturebackend.manager.upload.PictureUploadTemplate;
import com.katomegumi.zxpicturebackend.manager.upload.PictureUrlUpload;
import com.katomegumi.zxpicturebackend.manager.upload.modal.UploadPictureResult;
import com.katomegumi.zxpicturebackend.model.dao.entity.*;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureInfoMapper;
import com.katomegumi.zxpicturebackend.model.dao.mapper.UserInfoMapper;
import com.katomegumi.zxpicturebackend.model.dto.picture.*;
import com.katomegumi.zxpicturebackend.model.enums.*;
import com.katomegumi.zxpicturebackend.model.vo.picture.*;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceDetailVO;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceTeamDetailVO;
import com.katomegumi.zxpicturebackend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【picture_info(图片信息表)】的数据库操作Service实现
 * @createDate 2025-05-24 14:26:44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureInfoMapper, PictureInfo>
        implements PictureService {

    private final UserService userService;

    private final UserInfoMapper userInfoMapper;

    private final SpaceService spaceService;

    private final PictureCategoryService pictureCategoryService;

    private final PictureInteractionService pictureInteractionService;

    private final TransactionTemplate transactionTemplate;

    private final PictureUrlUpload pictureUrlUpload;

    private final PictureFileUpload pictureFileUpload;

    private final CapturePictureManager capturePictureManager;

    private final AsyncFileTaskHandler asyncFileTaskHandler;

    private final BaiLianApi baiLianApi;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PictureDetailVO uploadPicture(Object pictureInputSource, PictureUploadRequest pictureUploadRequest) {
        //1.获取登录用户
        UserInfo userInfo = userService.getCurrentUserInfo();

        //2.进行校验
        PictureInfo oldPictureInfo;
        PictureInfo newPictureInfo = new PictureInfo();
        String uploadPrefix;
        if (pictureUploadRequest != null) {
            //2.空间校验
            Long spaceId = pictureUploadRequest.getSpaceId();
            if (ObjUtil.isNotNull(spaceId) && !spaceId.equals(0L)) {
                SpaceInfo spaceInfo = spaceService.getById(spaceId);
                ThrowUtils.throwIf(spaceInfo == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
                //2.1.空间权限校验
                spaceService.checkSpaceAuth(spaceInfo, userInfo);
                //2.2 空间额度校验
                spaceService.checkSpaceAmount(spaceInfo);
                //2.3 根据空间存在与否构造 上传前缀
                uploadPrefix = PictureConstant.SPACE_PICTURE_PREFIX + spaceId;
                //2.4 设置空间Id
                newPictureInfo.setSpaceId(spaceId);
            } else {
                uploadPrefix = PictureConstant.PUBLIC_PICTURE_PREFIX + userInfo.getId();
                newPictureInfo.setSpaceId((long) SpaceTypeEnum.PUBLIC.getKey());
            }
            Long pictureId = pictureUploadRequest.getId();
            //3.根据图片id 判断是第一次上传 还是重新编辑
            if (ObjUtil.isNotNull(pictureId)) {
                oldPictureInfo = this.getById(pictureUploadRequest.getId());
                ThrowUtils.throwIf(oldPictureInfo == null, ErrorCode.PARAMS_ERROR, "图片不存在");
                //3.1 校验权限
                canOperatePicture(oldPictureInfo, userInfo);
                newPictureInfo.setId(oldPictureInfo.getId());
            } else {
                oldPictureInfo = null;
            }
        } else {
            oldPictureInfo = null;
            uploadPrefix = PictureConstant.PUBLIC_PICTURE_PREFIX + userInfo.getId();
        }

        //3.2 填充参数
        this.fillReviewParams(newPictureInfo, userInfo);

        //4.上传图片
        return transactionTemplate.execute(status -> {
            PictureUploadTemplate pictureUploadTemplate = pictureInputSource instanceof String ? pictureUrlUpload : pictureFileUpload;
            UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(pictureInputSource, uploadPrefix, true);
            BeanUtil.copyProperties(uploadPictureResult, newPictureInfo);
            //4.1保存图片信息
            boolean result = this.saveOrUpdate(newPictureInfo);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            //4.2如果有 清理老图片 在对象存储中的源
            if (oldPictureInfo != null) {
                asyncFileTaskHandler.clearPictureFile(oldPictureInfo);
                if (!oldPictureInfo.getSpaceId().equals(0L)) {
                    //说明是私有空间 需要更新额度()
                    spaceService.updateSpaceAmount(oldPictureInfo, false);
                }
            }
            //4.3新增 更新空间额度
            if (!newPictureInfo.getSpaceId().equals(0L)) {
                spaceService.updateSpaceAmount(newPictureInfo, true);
            }
            return PictureDetailVO.objToVo(this.getById(newPictureInfo.getId()));
        });
    }

    @Override
    public void deletePictureById(long pictureId) {
        // 判断是否存在
        PictureInfo oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        //校验是否有操作权限
        UserInfo userInfo = userService.getCurrentUserInfo();
        this.canOperatePicture(oldPicture, userInfo);

        // 开启事务
        transactionTemplate.execute(status -> {
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            Long spaceId = oldPicture.getSpaceId();
            if (!spaceId.equals(0L)) {
                spaceService.updateSpaceAmount(oldPicture, false);
            }
            return null;
        });

        asyncFileTaskHandler.clearPictureFile(oldPicture);
    }

    @Override
    @Transactional
    public void editPicture(PictureEditRequest pictureEditRequest) {
        // 判断是否存在
        long pictureId = pictureEditRequest.getId();
        PictureInfo oldPictureInfo = this.getById(pictureId);
        ThrowUtils.throwIf(oldPictureInfo == null, ErrorCode.PARAMS_ERROR, "图片不存在");

        PictureInfo pictureInfo = new PictureInfo();
        BeanUtils.copyProperties(pictureEditRequest, pictureInfo);
        // 注意将 list 转为 string
        pictureInfo.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        pictureInfo.setEditTime(new Date());

        UserInfo userInfo = userService.getCurrentUserInfo();
        //校验是否有权限操作
//        this.canOperatePicture(oldPictureInfo, userInfo);
        // 补充审核参数
        this.fillReviewParams(pictureInfo, userInfo);

        //判断图片分类是否存在 并且在使用上 use_num +1
        Long categoryId = pictureInfo.getCategoryId();
        if (categoryId != null) {
            // 分类Id是否存在?
            PictureCategory pictureCategory = pictureCategoryService.getById(pictureEditRequest.getCategoryId());
            ThrowUtils.throwIf(pictureCategory == null, ErrorCode.NOT_FOUND_ERROR, "分类不存在");
            pictureCategoryService.updateCategoryByPictureInfo(pictureInfo.getCategoryId(), oldPictureInfo);
        }

        // 操作数据库
        boolean result = this.updateById(pictureInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }


    @Override
    public PictureDetailVO getPictureDetailById(Long pictureId) {
        PictureInfo pictureInfo = this.getById(pictureId);
        ThrowUtils.throwIf(pictureInfo == null, ErrorCode.PARAMS_ERROR, "图片不存在");

        PictureDetailVO pictureDetailVO = PictureDetailVO.objToVo(pictureInfo);
        //获取对应创建用户信息
        UserInfo userInfo = userService.getById(pictureInfo.getUserId());
        pictureDetailVO.setUserName(userInfo.getName());
        pictureDetailVO.setUserAvatar(userInfo.getAvatar());

        //设置图片分类
        Long categoryId = pictureInfo.getCategoryId();
        if (categoryId != null) {
            PictureCategory pictureCategory = pictureCategoryService.getById(categoryId);
            pictureDetailVO.setCategoryName(pictureCategory.getName());
        }

        //如果登录 设置交互状态
        if (StpKit.USER.isLogin()) {
            List<PictureInteraction> pictureInteractionList = pictureInteractionService.getPictureInteractionListByPictureIdAndUserId(pictureId, StpKit.USER.getLoginIdAsLong());
            for (PictureInteraction pictureInteraction : pictureInteractionList) {
                if (pictureInteraction.getInteractionType().equals(PictureInteractionTypeEnum.LIKE.getKey())) {
                    pictureDetailVO.setIsLike(PictureInteractionStatusEnum.isInteracted(pictureInteraction.getInteractionStatus()));
                } else if (pictureInteraction.getInteractionType().equals(PictureInteractionTypeEnum.COLLECTION.getKey())) {
                    pictureDetailVO.setIsCollect(PictureInteractionStatusEnum.isInteracted(pictureInteraction.getInteractionStatus()));
                }
            }
        }
        //设置图片交互状态
        Map<Long, Integer> likeDeltas = getRedisDeltas(CacheConstant.PICTURE.PICTURE_INTERACTION_LIKE_KEY_PREFIX, List.of(pictureId));
        Map<Long, Integer> collectDeltas = getRedisDeltas(CacheConstant.PICTURE.PICTURE_INTERACTION_COLLECTION_KEY_PREFIX, List.of(pictureId));

        pictureDetailVO.setLikeQuantity(pictureDetailVO.getLikeQuantity() + likeDeltas.getOrDefault(pictureId, 0));
        pictureDetailVO.setCollectQuantity(pictureDetailVO.getCollectQuantity() + collectDeltas.getOrDefault(pictureId, 0));

        return pictureDetailVO;
    }

    @Override
    public PageVO<PictureHomeVO> getCollectPictureList(PictureQueryRequest pictureQueryRequest) {
        long userId = StpKit.USER.getLoginIdAsLong();
        List<Long> collectedPictureIds = pictureInteractionService.getCollectedPictureIds(userId);
        if (CollUtil.isEmpty(collectedPictureIds)) {
            return new PageVO<>();
        }
        LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = this.getLambdaQueryWrapper(pictureQueryRequest);
        lambdaQueryWrapper.in(PictureInfo::getId, collectedPictureIds);
        Page<PictureInfo> pictureInfoPage = this.page(pictureQueryRequest.getPage(PictureInfo.class), lambdaQueryWrapper);
        return new PageVO<>(
                pictureInfoPage.getCurrent(),
                pictureInfoPage.getSize(),
                pictureInfoPage.getTotal(),
                pictureInfoPage.getPages(),
                Optional.ofNullable(pictureInfoPage.getRecords())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(pictureInfo -> {
                            PictureHomeVO pictureHomeVO = BeanUtil.copyProperties(pictureInfo, PictureHomeVO.class);
                            pictureHomeVO.setIsLike(true);
                            return pictureHomeVO;
                        })
                        .collect(Collectors.toList())
        );
    }

    @Override
    public UserPictureStatsVO getUserPictureStats() {
        long userId = StpKit.USER.getLoginIdAsLong();
        UserPictureStatsVO userPictureStatsVO = new UserPictureStatsVO();
        Long uploadCount = this.lambdaQuery().eq(PictureInfo::getUserId, userId).count();
        userPictureStatsVO.setUploadCount(uploadCount);
        Long collectCount = pictureInteractionService.lambdaQuery()
                .eq(PictureInteraction::getUserId, userId)
                .eq(PictureInteraction::getInteractionType, PictureInteractionTypeEnum.COLLECTION.getKey())
                .eq(PictureInteraction::getInteractionStatus, PictureInteractionStatusEnum.INTERACTED.getKey())
                .count();
        userPictureStatsVO.setCollectCount(collectCount);
        return userPictureStatsVO;
    }

    @Override
    public PageVO<PictureVO> getPicturePageListAsPersonSpace(PictureQueryRequest pictureQueryRequest) {
        SpaceDetailVO spaceDetailVO = spaceService.getSpaceDetailByLoginUser();
        pictureQueryRequest.setSpaceId(spaceDetailVO.getId());
        LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = getLambdaQueryWrapper(pictureQueryRequest);
        Page<PictureInfo> pictureInfoPage = this.page(pictureQueryRequest.getPage(PictureInfo.class), lambdaQueryWrapper);
        PageVO<PictureVO> pictureVOPageVO = new PageVO<>(
                pictureInfoPage.getCurrent(),
                pictureInfoPage.getSize(),
                pictureInfoPage.getTotal(),
                pictureInfoPage.getPages(),
                Optional.ofNullable(pictureInfoPage.getRecords())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(pictureInfo -> BeanUtil.copyProperties(pictureInfo, PictureVO.class))
                        .collect(Collectors.toList())
        );
        List<PictureVO> pictureVOList = pictureVOPageVO.getRecords();
        if (CollUtil.isNotEmpty(pictureVOList)) {
            // 查询分类信息
            Set<Long> categoryIds = pictureVOList.stream()
                    .map(PictureVO::getCategoryId)
                    .collect(Collectors.toSet());
            Map<Long, PictureCategory> categoryMap = pictureCategoryService.selectMapByIds(categoryIds);
            pictureVOList.forEach(p -> {
                // 设置分类信息
                Long categoryId = p.getCategoryId();
                if (categoryMap.containsKey(categoryId)) {
                    p.setPictureCategory(categoryMap.get(categoryId));
                }
            });
        }
        return pictureVOPageVO;
    }

    @Override
    public PageVO<PictureVO> getPicturePageListAsTeamSpace(PictureQueryRequest pictureQueryRequest) {
        Long spaceId = pictureQueryRequest.getSpaceId();
        SpaceTeamDetailVO spaceTeamDetailVO;
        //不为用说明 是团队成员查询图片列表
        if (spaceId != null) {
            spaceService.existedSpaceBySpaceId(spaceId);
            SpaceInfo spaceInfo = spaceService.getById(spaceId);
            spaceService.checkSpaceAuth(spaceInfo, userService.getCurrentUserInfo());
            spaceTeamDetailVO = BeanUtil.copyProperties(spaceInfo, SpaceTeamDetailVO.class);
        } else {
            spaceTeamDetailVO = spaceService.getTeamSpaceDetailByLoginUser();
        }

        pictureQueryRequest.setSpaceId(spaceTeamDetailVO.getId());
        LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = getLambdaQueryWrapper(pictureQueryRequest);
        Page<PictureInfo> pictureInfoPage = this.page(pictureQueryRequest.getPage(PictureInfo.class), lambdaQueryWrapper);

        PageVO<PictureVO> pictureVOPageVO = new PageVO<>(
                pictureInfoPage.getCurrent(),
                pictureInfoPage.getSize(),
                pictureInfoPage.getTotal(),
                pictureInfoPage.getPages(),
                Optional.ofNullable(pictureInfoPage.getRecords())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(pictureInfo -> BeanUtil.copyProperties(pictureInfo, PictureVO.class))
                        .collect(Collectors.toList())
        );
        List<PictureVO> pictureVOList = pictureVOPageVO.getRecords();
        if (CollUtil.isNotEmpty(pictureVOList)) {
            // 查询分类信息
            Set<Long> categoryIds = pictureVOList.stream()
                    .map(PictureVO::getCategoryId)
                    .collect(Collectors.toSet());
            Map<Long, PictureCategory> categoryMap = pictureCategoryService.selectMapByIds(categoryIds);
            pictureVOList.forEach(p -> {
                // 设置分类信息
                Long categoryId = p.getCategoryId();
                if (categoryMap.containsKey(categoryId)) {
                    p.setPictureCategory(categoryMap.get(categoryId));
                }
            });
        }
        return pictureVOPageVO;
    }

    @Override
    public String pictureDownload(Long pictureId) {
        PictureInfo pictureInfo = this.getById(pictureId);
        ThrowUtils.throwIf(pictureInfo == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        return pictureInfo.getOriginUrl();
    }

    @Override
    public List<CapturePictureResult> capturePicture(capturePictureRequest capturePictureRequest) {
        String captureSource = capturePictureRequest.getCaptureSource();
        String searchText = capturePictureRequest.getSearchText();
        Integer captureCount = capturePictureRequest.getCaptureCount();
        Integer randomSeed = capturePictureRequest.getRandomSeed();
        //1.校验抓取源
        CaptureSourceEnum captureSourceEnum = CaptureSourceEnum.getEnumByKey(captureSource);
        if (captureSourceEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "抓取源不存在");
        }
        //2.调用抓取接口
        List<CapturePictureResult> capturePictureResults = capturePictureManager.capturePicture(captureSourceEnum.getUrl(), searchText, captureCount, randomSeed);

        //当 first为 1 时 count不生效了。这里需要截取
        if (capturePictureResults.size() > captureCount) {
            capturePictureResults = capturePictureResults.subList(0, captureCount);
        }

        //3.构建图片名称
        String namePrefix = capturePictureRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            //图片名称默认为 关键词
            namePrefix = searchText;
        }
        //4.保存图片名称
        AtomicInteger count = new AtomicInteger(1);
        String finalNamePrefix = namePrefix;
        capturePictureResults.forEach(capturePictureResult -> {
            capturePictureResult.setPictureName(String.format("%s_%d", finalNamePrefix, count.getAndIncrement()));
        });

        return capturePictureResults;
    }

    @Override
    public void uploadPictureByCapture(PictureUploadRequest pictureUploadRequest) {
        //构建前缀
        String prefixName = String.format("%s%s", PictureConstant.CAPTURE_PICTURE_PREFIX, DateUtil.formatDate(new Date()));
        UploadPictureResult uploadPictureResult = this.pictureUrlUpload.uploadPicture(pictureUploadRequest.getPictureUrl(), prefixName, true);
        PictureInfo pictureInfo = BeanUtil.copyProperties(uploadPictureResult, PictureInfo.class);
        long userId = StpKit.USER.getLoginIdAsLong();
        String pictureName = pictureUploadRequest.getPictureName();
        pictureInfo.setUserId(userId);
        pictureInfo.setPicName(pictureName);
        //上传数据库
        boolean result = this.save(pictureInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public List<SearchPictureResult> searchPictureByPicture(SearchPictureByPictureRequest searchPictureByPictureRequest) {
        PictureInfo pictureInfo = this.getById(searchPictureByPictureRequest.getPictureId());
        ThrowUtils.throwIf(pictureInfo == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        return ImageSearchApiFacade.searchImage(pictureInfo.getOriginUrl(), searchPictureByPictureRequest.getRandomSeed(), searchPictureByPictureRequest.getSearchCount());
    }

    public void canOperatePicture(PictureInfo oldPictureInfo, UserInfo userInfo) {
        if (userService.isAdmin(userInfo)) {
            return;
        }
        if (oldPictureInfo.getUserId().equals(userInfo.getId())) {
            return;
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限编辑");
    }


    @Override
    public void fillReviewParams(PictureInfo pictureInfo, UserInfo userInfo) {
        if (UserRoleEnum.isAdmin(userInfo.getRole())) {
            //管理直接放行
            pictureInfo.setReviewTime(new Date());
            pictureInfo.setReviewStatus(PictureReviewStatusEnum.PASS.getKey());
            pictureInfo.setReviewerId(userInfo.getId());
            pictureInfo.setReviewMessage("管理员直接过审");
            pictureInfo.setUserId(userInfo.getId());
        } else {
            //用户 设定为待审核
            pictureInfo.setReviewStatus(PictureReviewStatusEnum.REVIEW.getKey());
            pictureInfo.setUserId(userInfo.getId());
        }
    }

    @Override
    public void likeOrCollection(PictureInteractionRequest pictureInteractionRequest) {
        if (!StpKit.USER.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long pictureId = pictureInteractionRequest.getId();
        //图片是否存在
        existPictureById(pictureId);
        //修改互动表
        pictureInteractionService.changePictureLikeOrCollection(pictureInteractionRequest);

        //修改互动数据 存入redis
        Integer interactionType = pictureInteractionRequest.getInteractionType();
        //通过定时任务同步 到数据库
        stringRedisTemplate.opsForHash()
                .increment(PictureInteractionTypeEnum.LIKE.getKey().equals(interactionType) ? CacheConstant.PICTURE.PICTURE_INTERACTION_LIKE_KEY_PREFIX : CacheConstant.PICTURE.PICTURE_INTERACTION_COLLECTION_KEY_PREFIX,
                        pictureId.toString(),
                        PictureInteractionStatusEnum.isInteracted(pictureInteractionRequest.getInteractionStatus()) ? 1 : -1);
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureExtendTask(PictureExtendRequest pictureExtendRequest) {
        Long pictureId = pictureExtendRequest.getPictureId();
        PictureInfo pictureInfo = this.getById(pictureId);
        ThrowUtils.throwIf(pictureInfo == null, ErrorCode.OPERATION_ERROR, "图片不存在");
        CreateOutPaintingTaskRequest.Parameters parameters = pictureExtendRequest.getParameters();
        ThrowUtils.throwIf(parameters == null, ErrorCode.OPERATION_ERROR, "扩图参数为空");
        //使用redis 限制用户一天一次
        BoundSetOperations<String, String> setOperations = stringRedisTemplate.boundSetOps(CacheConstant.PICTURE.PICTURE_EXTEND_PREFIX);
        Long userId = StpKit.USER.getLoginIdAsLong();
        if (Boolean.TRUE.equals(setOperations.isMember(userId.toString()))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "一天只能扩图一次");
        }
        //否则添加 设置过期时间为每晚24点
        Date now = new Date();
        long seconds = Math.max(0, DateUtil.between(now, DateUtil.endOfDay(now), DateUnit.SECOND));
        setOperations.expire(seconds, TimeUnit.SECONDS);
        setOperations.add(userId.toString());
        CreateOutPaintingTaskRequest createOutPaintingTaskRequest = new CreateOutPaintingTaskRequest();
        createOutPaintingTaskRequest.setParameters(parameters);
        createOutPaintingTaskRequest.setInput(new CreateOutPaintingTaskRequest.Input(pictureInfo.getOriginUrl()));
        return baiLianApi.createOutPaintingTask(createOutPaintingTaskRequest);
    }

    @Override
    public GetOutPaintingTaskResponse queryPictureExtendTask(String taskId) {
        return baiLianApi.queryOutPaintingTask(taskId);
    }

    @Override
    public PageVO<PictureVO> searchPictureByPicColor(SearchPictureByColorRequest searchPictureByColorRequest) {
        UserInfo userInfo = userService.getCurrentUserInfo();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        String picColor = searchPictureByColorRequest.getPicColor();

        SpaceInfo spaceInfo = spaceService.getById(spaceId);
        if (ObjUtil.isNull(spaceInfo)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        ThrowUtils.throwIf(!spaceInfo.getUserId().equals(userInfo.getId()), ErrorCode.NO_AUTH_ERROR);
        //查看空间全部图(要有主色调的图)
        Page<PictureInfo> page = this.lambdaQuery()
                .eq(PictureInfo::getSpaceId, spaceId)
                .isNotNull(PictureInfo::getOriginColor)
                .page(searchPictureByColorRequest.getPage(PictureInfo.class));

        //将picColor(16进制) 转换RGB
        Color color = Color.decode(picColor);

        return new PageVO<>(page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                Optional.
                        ofNullable(page.getRecords()).
                        orElse(Collections.emptyList())
                        .stream()
                        .sorted(Comparator.comparingDouble(
                                (PictureInfo pictureInfo) -> {
                                    String hexColor = pictureInfo.getOriginColor();
                                    //没有主色调图片放最后
                                    if (StrUtil.isBlank(hexColor)) {
                                        return Double.MIN_VALUE;
                                    }
                                    //提取图片主色调
                                    Color originColor = Color.decode(hexColor);
                                    return ColorSimilarUtils.calculateSimilarity(color, originColor);
                                }
                        ).reversed())
                        .map(PictureVO::objToVo)
                        .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void reviewPicture(PictureReviewRequest pictureReviewRequest) {
        Long id = pictureReviewRequest.getId();
        List<Long> idList = pictureReviewRequest.getIdList();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();

        ArrayList<PictureInfo> pictureInfoList = new ArrayList<>();

        long userId = StpKit.USER.getLoginIdAsLong();

        if (id != null) {
            PictureInfo pictureInfo = new PictureInfo();
            pictureInfo.setId(id);
            pictureInfo.setReviewerId(userId);
            pictureInfo.setReviewMessage(reviewMessage);
            pictureInfo.setReviewStatus(reviewStatus);
            pictureInfoList.add(pictureInfo);
        } else {
            idList.forEach(pictureId -> {
                PictureInfo pictureInfo = new PictureInfo();
                pictureInfo.setId(pictureId);
                pictureInfo.setReviewerId(userId);
                pictureInfo.setReviewMessage(reviewMessage);
                pictureInfo.setReviewStatus(reviewStatus);
                pictureInfoList.add(pictureInfo);
            });
        }

        boolean result = this.updateBatchById(pictureInfoList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    @Transactional
    public void updatePicture(PictureUpdateRequest pictureUpdateRequest) {
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        PictureInfo oldPictureInfo = this.getById(id);
        ThrowUtils.throwIf(oldPictureInfo == null, ErrorCode.NOT_FOUND_ERROR);

        PictureInfo pictureInfo = new PictureInfo();
        BeanUtils.copyProperties(pictureUpdateRequest, pictureInfo);
        // 注意将 list 转为 string
        pictureInfo.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        //判断图片分类是否存在 并且在使用上 use_num +1
        Long categoryId = pictureInfo.getCategoryId();
        if (categoryId != null) {
            // 分类Id是否存在?
            PictureCategory pictureCategory = pictureCategoryService.getById(pictureUpdateRequest.getCategoryId());
            ThrowUtils.throwIf(pictureCategory == null, ErrorCode.NOT_FOUND_ERROR, "分类不存在");
            pictureCategoryService.updateCategoryByPictureInfo(pictureInfo.getCategoryId(), oldPictureInfo);
        }
        this.fillReviewParams(pictureInfo, userService.getCurrentUserInfo());
        boolean result = this.updateById(pictureInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public PageVO<PictureVO> getPicturePageListAsManage(PictureQueryRequest pictureQueryRequest) {
        LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = this.getLambdaQueryWrapper(pictureQueryRequest);
        Page<PictureInfo> page = this.page(pictureQueryRequest.getPage(PictureInfo.class), lambdaQueryWrapper);

        PageVO<PictureVO> pictureVOPageVO = new PageVO<>(page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                Optional.
                        ofNullable(page.getRecords()).
                        orElse(Collections.emptyList())
                        .stream()
                        .map(PictureVO::objToVo)
                        .collect(Collectors.toList()));

        List<PictureVO> pictureVOList = pictureVOPageVO.getRecords();

        if (CollUtil.isNotEmpty(pictureVOList)) {
            //获取图片用户信息
            Set<Long> userIds = pictureVOList.stream().map(PictureVO::getUserId).collect(Collectors.toSet());
            Map<Long, UserInfo> userInfoMap = userInfoMapper.selectMapByIds(userIds);
            //获取图片分类信息
            Set<Long> categoryIds = pictureVOList.stream().map(PictureVO::getCategoryId).collect(Collectors.toSet());
            Map<Long, PictureCategory> pictureCategoryMap;
            if (CollUtil.isNotEmpty(categoryIds)) {
                pictureCategoryMap = pictureCategoryService.selectMapByIds(categoryIds);
            } else {
                pictureCategoryMap = Collections.emptyMap();
            }
            //填充分类 用户信息
            pictureVOList.forEach(p -> {
                Long userId = p.getUserId();
                if (userInfoMap.containsKey(userId)) {
                    p.setUserInfo(userInfoMap.get(userId));
                }
                if (CollUtil.isEmpty(pictureCategoryMap)) {
                    return;
                }
                Long categoryId = p.getCategoryId();
                if (ObjUtil.isNotNull(categoryId)) {
                    p.setPictureCategory(pictureCategoryMap.get(categoryId));
                }
            });

            pictureVOPageVO.setRecords(pictureVOList);
        }

        return pictureVOPageVO;
    }

    @Override
    public void existPictureById(Long pictureId) {
        if (this.exists(new LambdaQueryWrapper<PictureInfo>().eq(PictureInfo::getId, pictureId))) return;
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不存在");
    }


    @Override
    public Map<Long, Integer> getRedisDeltas(String redisKey, List<Long> pictureIds) {
        // 将Long类型的ID转换为Object类型（Redis需要的key格式）
        List<Object> keys = pictureIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());

        // 批量获取Redis中的变化量
        List<Object> values = stringRedisTemplate.opsForHash()
                .multiGet(redisKey, keys);

        if (CollUtil.isEmpty(values)) {
            return Collections.emptyMap();
        }

        // 构建ID到变化量的映射
        Map<Long, Integer> result = new HashMap<>();
        for (int i = 0; i < pictureIds.size(); i++) {
            Long id = pictureIds.get(i);
            Object value = values.get(i);
            int delta = 0;

            if (value != null) {
                try {
                    delta = Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    log.error("解析Redis变化量失败: key={}, value={}", redisKey, value, e);
                }
            }

            result.put(id, delta);
        }

        return result;
    }

    /**
     * 构造请求参数
     *
     * @param pictureQueryRequest 请求参数
     * @return LambdaQueryWrapper
     */
    @Override
    @SneakyThrows(ParseException.class)
    public LambdaQueryWrapper<PictureInfo> getLambdaQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        String searchText = pictureQueryRequest.getSearchText();
        Long id = pictureQueryRequest.getId();
        String originFormat = pictureQueryRequest.getOriginFormat();
        Integer originWidth = pictureQueryRequest.getOriginWidth();
        Integer originHeight = pictureQueryRequest.getOriginHeight();
        Double originScale = pictureQueryRequest.getOriginScale();
        String originColor = pictureQueryRequest.getOriginColor();
        String picName = pictureQueryRequest.getPicName();
        String picDesc = pictureQueryRequest.getPicDesc();
        Long categoryId = pictureQueryRequest.getCategoryId();
        List<String> tags = pictureQueryRequest.getTags();
        Long userId = pictureQueryRequest.getUserId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String sortField = pictureQueryRequest.getSortField();
        Boolean sortOrder = pictureQueryRequest.getSortOrder();

        LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper
                //匹配 图片名称 图片简介 图片标签(一个满足即可)
                .and(StrUtil.isNotBlank(searchText), lqw ->
                        lqw.and(wrapper ->
                                wrapper.like(PictureInfo::getPicName, searchText)
                                        .or()
                                        .like(PictureInfo::getPicDesc, searchText)
                                        .or()
                                        .apply("FIND_IN_SET ('" + searchText + "', tags) > 0")
                        )
                )
                .eq(ObjUtil.isNotNull(id), PictureInfo::getId, id)
                .eq(StrUtil.isNotBlank(originFormat), PictureInfo::getOriginFormat, originFormat)
                .eq(ObjUtil.isNotNull(originWidth), PictureInfo::getOriginWidth, originWidth)
                .eq(ObjUtil.isNotNull(originHeight), PictureInfo::getOriginHeight, originHeight)
                .eq(ObjUtil.isNotNull(originScale), PictureInfo::getOriginScale, originScale)
                .eq(StrUtil.isNotBlank(originColor), PictureInfo::getOriginColor, originColor)
                .like(StrUtil.isNotBlank(picName), PictureInfo::getPicName, picName)
                .like(StrUtil.isNotBlank(picDesc), PictureInfo::getPicDesc, picDesc)
                .eq(ObjUtil.isNotNull(categoryId), PictureInfo::getCategoryId, categoryId)
                .eq(ObjUtil.isNotNull(userId), PictureInfo::getUserId, userId)
                .eq(ObjUtil.isNotNull(spaceId), PictureInfo::getSpaceId, spaceId)
                .eq(ObjUtil.isNotNull(reviewerId), PictureInfo::getReviewerId, reviewerId)
                .eq(ObjUtil.isNotNull(reviewStatus), PictureInfo::getReviewStatus, reviewStatus)
                .like(StrUtil.isNotBlank(reviewMessage), PictureInfo::getReviewMessage, reviewMessage);

        //构造 时间
        if (StrUtil.isNotBlank(pictureQueryRequest.getStartEditTime()) && StrUtil.isNotBlank(pictureQueryRequest.getEndEditTime())) {
            Date startEditTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(pictureQueryRequest.getStartEditTime());
            Date endEditTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(pictureQueryRequest.getEndEditTime());
            lambdaQueryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), PictureInfo::getEditTime, startEditTime);
            lambdaQueryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), PictureInfo::getEditTime, endEditTime);
        }

        //拼接tag标签
        //拼接一下tag标签
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                lambdaQueryWrapper.like(PictureInfo::getTags, "\"" + tag + "\"");
            }
        }

        //构造排序
        if (sortField != null) {
            lambdaQueryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder, SFunctionUtils.getSFunction(PictureInfo.class, sortField));
        } else {
            lambdaQueryWrapper.orderByDesc(PictureInfo::getCreateTime);
        }
        return lambdaQueryWrapper;
    }
}





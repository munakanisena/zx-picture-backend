package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.AliYunAiApi;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.manager.cos.CosManager;
import com.katomegumi.zxpicturebackend.manager.upload.PictureFileUpload;
import com.katomegumi.zxpicturebackend.manager.upload.PictureUploadTemplate;
import com.katomegumi.zxpicturebackend.manager.upload.PictureUrlUpload;
import com.katomegumi.zxpicturebackend.manager.upload.modal.UploadPictureResult;
import com.katomegumi.zxpicturebackend.model.dto.picture.*;
import com.katomegumi.zxpicturebackend.model.dao.entity.Picture;
import com.katomegumi.zxpicturebackend.model.dao.entity.Space;
import com.katomegumi.zxpicturebackend.model.dao.entity.User;
import com.katomegumi.zxpicturebackend.model.enums.PictureReviewStatusEnum;
import com.katomegumi.zxpicturebackend.model.vo.PictureVO;
import com.katomegumi.zxpicturebackend.model.vo.user.UserDetailVO;
import com.katomegumi.zxpicturebackend.service.PictureService;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureMapper;
import com.katomegumi.zxpicturebackend.service.SpaceService;
import com.katomegumi.zxpicturebackend.service.UserService1;
import com.katomegumi.zxpicturebackend.core.util.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author lirui
* @description 针对表【tb_picture(图片)】的数据库操作Service实现
* @createDate 2025-02-15 19:51:44
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService1 userService1;

    @Resource
    private PictureFileUpload pictureFileUpload;

    @Resource
    private PictureUrlUpload pictureUrlUpload;

    @Autowired
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        return null;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        return null;
    }

    //    /**
//     * 上传图片
//     *
//     * @param inputSource
//     * @param pictureUploadRequest
//     * @param loginUser
//     * @return
//     */
//    @Override
//    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
//        //进行校验
//        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
//        //空间校验
//        Long spaceId = pictureUploadRequest.getSpaceId();
//        if (spaceId != null) {
//            Space space = spaceService.getById(spaceId);
//            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
//            // 必须空间创建人（管理员）才能上传
//            if (!loginUser.getId().equals(space.getUserId())) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
//            }
//            //校验额度
//            if (space.getTotalCount() >= space.getMaxCount()) {
//                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
//            }
//            if (space.getTotalSize() >= space.getMaxSize()) {
//                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
//            }
//        }
//        //这个picture用于判断是新增还是 更新
//        Long pictureId = null;
//        if (pictureUploadRequest != null) {
//            pictureId = pictureUploadRequest.getId();
//        }
//        if (pictureId != null) {
//            Picture oldPicture = this.getById(pictureId);
//            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
//            //是本人还是 管理员操作
//            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService1.isAdmin(loginUser)) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//            }
//          /*  boolean result = lambdaQuery().eq(Picture::getId, pictureId)
//                    .eq(Picture::getUserId, loginUser.getId())
//                    .exists();
//            ThrowUtils.throwIf(!result,ErrorCode.NOT_FOUND_ERROR,"图片不存在");*/
//            // 校验空间是否一致
//            // 没传 spaceId，则复用原有图片的 spaceId
//            if (spaceId == null) {
//                if (oldPicture.getSpaceId() != null) {
//                    spaceId = oldPicture.getSpaceId();
//                }
//            } else {
//                // 传了 spaceId，必须和原有图片一致
//                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
//                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
//                }
//            }
//        }
//        //上传目录 判断是公共还是私人空间的使用
//        String prefix;
//        if (spaceId==null) {
//            prefix = String.format("public/%s", loginUser.getId());
//        }else {
//            prefix=String.format("space/%s", spaceId);
//        }
//        //需要进行区分 是传的图片还是URL
//        PictureUploadTemplate pictureUploadTemplate = pictureFileUpload;
//        if (inputSource instanceof String) {
//            pictureUploadTemplate = pictureUrlUpload;
//        }
//        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, prefix);
//
//        Picture picture = new Picture();
//        picture.setSpaceId(spaceId);
//        picture.setUrl(uploadPictureResult.getUrl());
//        //
//        String picName = uploadPictureResult.getPicName();
//        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
//            picName = pictureUploadRequest.getPicName();
//        }
//        picture.setName(picName);
//        picture.setPicSize(uploadPictureResult.getPicSize());
//        picture.setPicWidth(uploadPictureResult.getPicWidth());
//        picture.setPicHeight(uploadPictureResult.getPicHeight());
//        picture.setPicScale(uploadPictureResult.getPicScale());
//        picture.setPicFormat(uploadPictureResult.getPicFormat());
//        picture.setUserId(loginUser.getId());
//        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
//        picture.setPicColor(uploadPictureResult.getPicColor());
//        //补充参数
//        this.fillReviewParams(picture, loginUser);
//
//        // 如果 pictureId 不为空，表示更新，否则是新增
//        if (pictureId != null) {
//            // 如果是更新，需要补充 id 和编辑时间
//            picture.setId(pictureId);
//            picture.setEditTime(new Date());
//        }
//        //保存或者更新 如果传入的对象（如 picture）没有主键 或主键对应的记录在数据库中 不存在，则执行 save（插入新记录）。
//        //如果传入的对象 有主键 且主键对应的记录在数据库中 已存在，则执行 update（更新记录）。
//        Long finalSpaceId = spaceId;
//        transactionTemplate.execute(status -> {
//            boolean result = this.saveOrUpdate(picture);
//            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
//            //采用上传后再进行计算的方式   根据需求是否精确实现要求
//            if (finalSpaceId!=null) {
//                boolean update = spaceService.lambdaUpdate()
//                        .eq(Space::getId, finalSpaceId)
//                        .setSql("totalSize= totalSize +" + picture.getPicSize())
//                        .setSql("totalCount=totalCount+1")
//                        .update();
//                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新图库空间失败");
//            }
//            return null;
//        }
//        );
//        return PictureVO.objToVo(picture);
//    }
//
//    @Override
//    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
//        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
//        if (pictureQueryRequest == null) {
//            return queryWrapper;
//        }
//        Long id = pictureQueryRequest.getId();
//        String name = pictureQueryRequest.getName();
//        String introduction = pictureQueryRequest.getIntroduction();
//        String category = pictureQueryRequest.getCategory();
//        List<String> tags = pictureQueryRequest.getTags();
//        Long picSize = pictureQueryRequest.getPicSize();
//        Integer picWidth = pictureQueryRequest.getPicWidth();
//        Integer picHeight = pictureQueryRequest.getPicHeight();
//        Double picScale = pictureQueryRequest.getPicScale();
//        String picFormat = pictureQueryRequest.getPicFormat();
//        String searchText = pictureQueryRequest.getSearchText();
//        Long userId = pictureQueryRequest.getUserId();
//        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
//        String reviewMessage = pictureQueryRequest.getReviewMessage();
//        Long reviewerId = pictureQueryRequest.getReviewerId();
//        String sortField = pictureQueryRequest.getSortField();
//        String sortOrder = pictureQueryRequest.getSortOrder();
//        Long spaceId = pictureQueryRequest.getSpaceId();//进行模糊搜索
//        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
//        Date startEditTime = pictureQueryRequest.getStartEditTime();
//        Date endEditTime = pictureQueryRequest.getEndEditTime();
//
//        if (searchText != null) {
//            queryWrapper.and(
//                            wrapper -> wrapper.like("name", searchText))
//                    .or()
//                    .like("introduction", searchText);
//        }
//        //通过传来的请求参数 构造queryWrapper
//        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
//        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
//        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
//        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
//        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
//        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
//        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
//        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
//        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
//        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
//        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
//        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
//        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
//        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
//        queryWrapper.isNull(nullSpaceId, "spaceId");
//
//        //构造时间  >= 开始时间
//        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "startEditTime", startEditTime);
//        //        <=结束时间
//        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "endEditTime", endEditTime);
//
//        //拼接一下tag标签
//        if (CollUtil.isNotEmpty(tags)) {
//            for (String tag : tags) {
//                queryWrapper.like("tags", "\"" + tag + "\"");
//            }
//        }
//        // 排序
//        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
//        return queryWrapper;
//    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService1.getById(userId);
            UserDetailVO userDetailVO = userService1.getUserVO(user);
            pictureVO.setUser(userDetailVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> records = picturePage.getRecords();
        //设定
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(records)) {
            return pictureVOPage;
        }

        List<PictureVO> pictureVOList = records.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        //为了获得用户的详细信息
        Set<Long> userIds = records.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService1.listByIds(userIds).stream().collect(Collectors.groupingBy(User::getId));

        for (PictureVO pictureVO : pictureVOList) {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService1.getUserVO(user));
        }
        return pictureVOPage.setRecords(pictureVOList);
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //改用为注解校验
        //checkPictureAuth(loginUser, oldPicture);
        // 开启事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            // 释放额度
            Long spaceId = oldPicture.getSpaceId();
            if (spaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return null;
        });
        // 异步清理文件
        this.clearPictureFile(oldPicture);
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        //改用为注解校验
        // checkPictureAuth(loginUser, oldPicture);
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //校验请求参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        ThrowUtils.throwIf(id == null || statusEnum == null, ErrorCode.PARAMS_ERROR);
        //判断图片是否存在
        Picture picture = getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //状态是否重复
        ThrowUtils.throwIf(picture.getReviewStatus().equals(statusEnum.getValue()), ErrorCode.PARAMS_ERROR, "重复审核");
        //更新图片
        Picture updatePicture = new Picture(); //为什么new一个新对象？ 因为updateById是全部字段进行更新 如果是旧的 每个字段都会更新一遍
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR);
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService1.isAdmin(loginUser)) {
            //管理直接放行
            picture.setReviewTime(new Date());
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员直接过审");
        } else {
            //用户 设定为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEW.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多抓取30条");
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        // 要抓取的地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        //通过接口 获取图片
        Document document;
        //对图片URL进行校验 修改
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取html失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements elementList = div.select("img.mimg");
        int uploadCount = 0;
        //解析图片
        for (Element igmElement : elementList) {
            String fileUrl = igmElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            //修改路径 把不要的去掉
            int index = fileUrl.indexOf("?");
            if (index > -1) {
                fileUrl = fileUrl.substring(0, index);
            }
            //上传对象存储
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if (StrUtil.isNotBlank(namePrefix)) {
                //按序号递增
                pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            }
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功:{}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.info("图片上传失败");
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        //返回批量生成多少tup
        return uploadCount;
    }

    @Override
    @Async
    public void clearPictureFile(Picture picture) {
        String url = picture.getUrl();
        Long count = this.lambdaQuery()
                .eq(Picture::getUrl, url)
                .count();
        //存在多条引用 就不进行删除
        if (count > 1) {
            return;
        }
        //删除前域名
        int i = url.indexOf("/");
        String keyUrl = url.substring(i);
        //使用对象存储进行删除
        cosManager.deleteObject(keyUrl);

        String thumbnailUrl = picture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            int i1 = thumbnailUrl.indexOf("/");
            cosManager.deleteObject(thumbnailUrl.substring(i1));
        }
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService1.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }


    @Override
    public List<PictureVO> searchPictureByPicColor(Long spaceId,String picColor,User loginUser){
        //校验参数
        ThrowUtils.throwIf(spaceId==null||StrUtil.isBlank(picColor),ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(spaceId);
        if (ObjUtil.isNull(space)) {
          throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()),ErrorCode.NO_AUTH_ERROR);
        //查看空间全部图(要有主色调的图)
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        if (pictureList.isEmpty()){
            return Collections.emptyList();
        }
        //将picColor(16进制) 转换RGB
        Color color =Color.decode(picColor);
        //调用工具类 比较相似度
        List<Picture> pictureList1 = pictureList.stream()
                .sorted(Comparator.comparingDouble(
                        picture -> {
                            String hexColor = picture.getPicColor();
                            //没有主色调图片放最后
                            if (StrUtil.isBlank(hexColor)) {
                                return Double.MAX_VALUE;
                            }
                            //提取图片主色调
                            Color color1 = Color.decode(hexColor);
                            return -ColorSimilarUtils.calculateSimilarity(color, color1);
                        }
                )).limit(12).collect(Collectors.toList());//取前12条
        //转换为 pictureVO返回
            return pictureList1.stream()
                    .map(PictureVO::objToVo)
                    .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        //校验传来的参数
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureIdList),ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NO_AUTH_ERROR);
        //校验空间 用户权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space==null,ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()),ErrorCode.NO_AUTH_ERROR,"没有空间权限");
        //从数据库中取图片
        List<Picture> pictureList = this.lambdaQuery().select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();

        if (CollUtil.isEmpty(pictureList)){
            return;
        }
        //更新 分类和标签
        pictureList.forEach(picture -> {
            if (StrUtil.isNotBlank(category)){
                picture.setCategory(category);
            }
            if (ObjUtil.isNotEmpty(tags)){
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });
        // 批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList, nameRule);
        //更新数据库
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"更新失败");
    }

    /**
     * nameRule 格式：图片{序号}
     * @param pictureList
     * @param nameRule
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count=1;
        try {
            for (Picture picture : pictureList) {
                String name = nameRule.replaceAll("\\{字符}", String.valueOf(count++));
                picture.setName(name);
            }
        } catch (Exception e) {
            log.error("名称解析错误",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"名称解析错误");
        }
    }


    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        //新的抛出异常方式
        Picture picture = Optional.ofNullable(this.getById(pictureId)) //如果空 就会执行后面的抛出异常操作
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        // 权限校验
        //改用为注解校验
        checkPictureAuth(loginUser, picture);
        // 构造请求参数
        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        taskRequest.setInput(input);
        BeanUtil.copyProperties(createPictureOutPaintingTaskRequest, taskRequest);

        return aliYunAiApi.createOutPaintingTask(taskRequest);
    }

}





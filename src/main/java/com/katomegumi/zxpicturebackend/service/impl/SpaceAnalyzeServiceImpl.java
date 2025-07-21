package com.katomegumi.zxpicturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.exception.ThrowUtils;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureCategory;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureCategoryMapper;
import com.katomegumi.zxpicturebackend.model.dao.mapper.PictureInfoMapper;
import com.katomegumi.zxpicturebackend.model.dao.mapper.SpaceInfoMapper;
import com.katomegumi.zxpicturebackend.model.dto.space.analyze.*;
import com.katomegumi.zxpicturebackend.model.enums.PictureSizeRangeEnum;
import com.katomegumi.zxpicturebackend.model.enums.SpaceTypeEnum;
import com.katomegumi.zxpicturebackend.model.vo.space.analyze.*;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceVO;
import com.katomegumi.zxpicturebackend.service.SpaceAnalyzeService;
import com.katomegumi.zxpicturebackend.service.SpaceService;
import com.katomegumi.zxpicturebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lirui
 */
@Service
@RequiredArgsConstructor
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceInfoMapper, SpaceInfo>
        implements SpaceAnalyzeService {

    private final SpaceInfoMapper spaceInfoMapper;

    private final UserService userService;

    private final SpaceService spaceService;

    private final PictureInfoMapper pictureInfoMapper;

    private final PictureCategoryMapper pictureCategoryMapper;

    /**
     * 拼接查询Query的范围
     *
     * @param spaceAnalyzeRequest 空间分析请求
     * @param queryWrapper        查询条件
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<PictureInfo> queryWrapper) {
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.eq("space_id", SpaceTypeEnum.PUBLIC.getKey());
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("space_id", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }

    @Override
    public SpaceUsageAnalyzeResponse analyzeSpaceUsed(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest) {
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            checkSpaceAnalyzeParam(spaceUsageAnalyzeRequest);
            LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            //这里我们只分析原图信息 压缩图.缩略图不再范围内
            lambdaQueryWrapper.select(PictureInfo::getOriginSize);
            //补充查询参数
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, lambdaQueryWrapper);

            List<Object> sizeList = pictureInfoMapper.selectObjs(lambdaQueryWrapper);
            long usedSize = sizeList
                    .stream()
                    .mapToLong(result -> result instanceof Long ? (Long) result : 0)
                    .sum();
            long usedCount = sizeList.size();

            // 封装返回结果
            return SpaceUsageAnalyzeResponse
                    .builder()
                    .usedSize(usedSize)
                    .usedCount(usedCount)
                    .maxSize(null)
                    .sizeUsageRatio(null)
                    .maxCount(null)
                    .countUsageRatio(null)
                    .build();
        } else {
            // 否则是分析指定空间
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            this.checkSpaceAnalyzeParam(spaceUsageAnalyzeRequest);

            SpaceInfo spaceInfo = spaceInfoMapper.selectById(spaceId);
            return SpaceUsageAnalyzeResponse
                    .builder()
                    .usedSize(spaceInfo.getUsedSize())
                    .maxSize(spaceInfo.getMaxSize())
                    .usedCount(spaceInfo.getUsedCount())
                    .maxCount(spaceInfo.getMaxCount())
                    .sizeUsageRatio(NumberUtil.round(spaceInfo.getUsedSize() * 100.0 / spaceInfo.getMaxSize(), 2).doubleValue())
                    .countUsageRatio(NumberUtil.round(spaceInfo.getUsedCount() * 100.0 / spaceInfo.getMaxCount(), 2).doubleValue())
                    .build();
        }
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> analyzeSpaceCategory(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest) {
        checkSpaceAnalyzeParam(spaceCategoryAnalyzeRequest);
        QueryWrapper<PictureInfo> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);

        // 分组查询 需要三个字段
        queryWrapper.select("category_id  ",
                        "COUNT(*) AS count",
                        "SUM(origin_size) AS totalSize")
                .groupBy("category_id");

        // 查询并转换结果
        return pictureInfoMapper
                .selectMaps(queryWrapper)
                .stream()
                .map(map -> {
                    String categoryName;
                    long count;
                    if (map.get("category_id") != null) {
                        PictureCategory pictureCategory = pictureCategoryMapper
                                .selectOne(new LambdaQueryWrapper<PictureCategory>().eq(PictureCategory::getId, map.get("category_id")));
                        categoryName = pictureCategory.getName();
                    } else {
                        categoryName = "未分类";
                    }
                    count = ((Number) map.get("count")).longValue();
                    Long totalSize = ((Number) map.get("totalSize")).longValue();
                    return SpaceCategoryAnalyzeResponse.builder()
                            .categoryName(categoryName)
                            .count(count)
                            .totalSize(totalSize)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> analyzeSpaceTags(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest) {
        checkSpaceAnalyzeParam(spaceTagAnalyzeRequest);
        LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, lambdaQueryWrapper);

        lambdaQueryWrapper.select(PictureInfo::getTags);
        List<String> strJsonList = pictureInfoMapper.selectObjs(lambdaQueryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(ObjUtil::toString)
                .toList();

        //合并全部的tag
        Map<String, Long> stringLongMap = strJsonList
                .stream()
                //将单个json格式字符串变为string list  然后合成为一个流
                .flatMap(strJson -> JSONUtil.toList(strJson, String.class).stream())
                //统计标签数量
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        //最后按照降序排序
        return stringLongMap
                .entrySet()
                .stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> SpaceTagAnalyzeResponse
                        .builder()
                        .tagName(entry.getKey())
                        .count(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> analyzeSpaceSize(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest) {
        checkSpaceAnalyzeParam(spaceSizeAnalyzeRequest);

        LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, lambdaQueryWrapper);

        lambdaQueryWrapper.select(PictureInfo::getOriginSize);
        List<Long> picSizes = pictureInfoMapper.selectObjs(lambdaQueryWrapper)
                .stream()
                .map(size -> ((Number) size).longValue())
                .toList();

        long[] counts = new long[PictureSizeRangeEnum.values().length];

        for (Long size : picSizes) {
            if (size < PictureSizeRangeEnum.LESS_THAN_1MB.getEnd()) {
                counts[0]++;
            } else if (size < PictureSizeRangeEnum.BETWEEN_1MB_10MB.getEnd()) {
                counts[1]++;
            } else if (size < PictureSizeRangeEnum.BETWEEN_10MB_20MB.getEnd()) {
                counts[2]++;
            } else {
                counts[3]++;
            }
        }

        // 定义分段范围，注意使用有序 Map
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put(PictureSizeRangeEnum.LESS_THAN_1MB.getLabel(), counts[0]);
        sizeRanges.put(PictureSizeRangeEnum.BETWEEN_1MB_10MB.getLabel(), counts[1]);
        sizeRanges.put(PictureSizeRangeEnum.BETWEEN_10MB_20MB.getLabel(), counts[2]);
        sizeRanges.put(PictureSizeRangeEnum.GREATER_THAN_20MB.getLabel(), counts[3]);

        return sizeRanges
                .entrySet()
                .stream()
                .map(entry -> SpaceSizeAnalyzeResponse.builder()
                        .sizeRange(entry.getKey())
                        .count(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> analyzeSpaceUserAction(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest) {
        //用户行为无需空间校验校验 但需要判断是否为本人或者管理员
        UserInfo userInfo = userService.getCurrentUserInfo();
        Long userId = spaceUserAnalyzeRequest.getUserId();
        ThrowUtils.throwIf(!userInfo.getId().equals(userId) && !userService.isAdmin(userInfo), ErrorCode.NO_AUTH_ERROR, "非本人操作");

        QueryWrapper<PictureInfo> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(ObjUtil.isNotNull(userId), "user_id", userId);

        // 分析维度：每日、每周、每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%m-%d') AS period", "COUNT(*) AS count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(create_time) AS period", "COUNT(*) AS count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%m') AS period", "COUNT(*) AS count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }

        // 分组和排序
        queryWrapper.groupBy("period").orderByAsc("period");

        // 查询结果并转换
        List<Map<String, Object>> queryResult = pictureInfoMapper.selectMaps(queryWrapper);
        return queryResult.stream()
                .map(result -> {
                    String period = result.get("period").toString();
                    Long count = ((Number) result.get("count")).longValue();
                    return SpaceUserAnalyzeResponse
                            .builder()
                            .period(period)
                            .count(count)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceVO> analyzeSpaceRank() {
        LambdaQueryWrapper<SpaceInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .select(SpaceInfo::getId, SpaceInfo::getSpaceName, SpaceInfo::getUserId, SpaceInfo::getUsedSize)
                .orderByDesc(SpaceInfo::getUsedSize)
                .last("Limit " + 10);
        return spaceInfoMapper
                .selectList(lambdaQueryWrapper)
                .stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
    }

    /**
     * 校验空间分析请求参数
     *
     * @param spaceAnalyzeRequest 分析图库请求
     */
    private void checkSpaceAnalyzeParam(SpaceAnalyzeRequest spaceAnalyzeRequest) {
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryAll || queryPublic) {
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");
        SpaceInfo spaceInfo = spaceInfoMapper.selectById(spaceId);
        ThrowUtils.throwIf(spaceInfo == null, ErrorCode.PARAMS_ERROR, "空间不存在");
        spaceService.checkSpaceAuth(spaceInfo, userService.getCurrentUserInfo());
    }

    /**
     * 拼接空间分析的查询范围
     *
     * @param spaceAnalyzeRequest 空间分析请求
     * @param lambdaQueryWrapper  查询条件
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper) {
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        if (spaceAnalyzeRequest.isQueryPublic()) {
            lambdaQueryWrapper.eq(PictureInfo::getSpaceId, SpaceTypeEnum.PUBLIC.getKey());
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            lambdaQueryWrapper.eq(PictureInfo::getSpaceId, spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }

}





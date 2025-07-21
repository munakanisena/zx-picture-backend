package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.model.dao.entity.SpaceInfo;
import com.katomegumi.zxpicturebackend.model.dto.space.analyze.*;
import com.katomegumi.zxpicturebackend.model.vo.space.analyze.*;
import com.katomegumi.zxpicturebackend.model.vo.space.info.SpaceVO;

import java.util.List;

/**
 * @author Megumi
 * @description 图库分析接口
 */
public interface SpaceAnalyzeService extends IService<SpaceInfo> {

    /**
     * 分析空间内图片使用情况
     *
     * @param spaceUsageAnalyzeRequest 空间使用分析请求
     * @return 空间使用情况
     */
    SpaceUsageAnalyzeResponse analyzeSpaceUsed(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest);

    /**
     * 分析空间内图片分类 的数量和大小
     *
     * @param spaceCategoryAnalyzeRequest 空间分类分析请求
     * @return 空间分类分析结果
     */
    List<SpaceCategoryAnalyzeResponse> analyzeSpaceCategory(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest);

    /**
     * 分析空间内图片的标签出现次数
     *
     * @param spaceTagAnalyzeRequest 空间标签分析请求
     * @return 空间标签分析结果
     */
    List<SpaceTagAnalyzeResponse> analyzeSpaceTags(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest);

    /**
     * 分析空间内图片的大小范围
     *
     * @param spaceSizeAnalyzeRequest 空间大小分析请求
     * @return 空间大小分析结果
     */
    List<SpaceSizeAnalyzeResponse> analyzeSpaceSize(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest);

    /**
     * 分析用户上传行为
     *
     * @param spaceUserAnalyzeRequest 用户上传行为分析请求
     * @return 用户上传行为分析结果
     */
    List<SpaceUserAnalyzeResponse> analyzeSpaceUserAction(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest);

    /**
     * 按存储使用量排序查询 获取前10
     *
     * @return 空间排名分析结果
     */
    List<SpaceVO> analyzeSpaceRank();

}

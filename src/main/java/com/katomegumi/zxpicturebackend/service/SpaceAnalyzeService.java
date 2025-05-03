package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.SpaceSizeAnalyzeRequest;
import com.katomegumi.zxpicturebackend.model.dto.space.analyze.*;
import com.katomegumi.zxpicturebackend.model.vo.space.analyze.*;
import com.katomegumi.zxpicturebackend.model.dao.entity.Space;
import com.katomegumi.zxpicturebackend.model.dao.entity.User;

import java.util.List;

/**
 * 图库分析接口
 */
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 图库资源分析解救
     * @param spaceUsageAnalyzeRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 根据图片 分类 获取各个不同分类的图片总数 和图片总大小
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 图片标签 出现次数
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 根据图片大小进行分类
     * @param spaceSizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 分析用户上传行为
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 按存储使用量排序查询(仅管理员可用)
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);

}

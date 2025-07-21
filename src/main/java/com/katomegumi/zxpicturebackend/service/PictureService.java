package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.core.api.search.model.SearchPictureResult;
import com.katomegumi.zxpicturebackend.core.common.resp.PageVO;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.katomegumi.zxpicturebackend.model.dto.picture.*;
import com.katomegumi.zxpicturebackend.model.vo.picture.*;
import lombok.SneakyThrows;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author lirui
 * @description 针对表【picture_info(图片信息表)】的数据库操作Service
 * @createDate 2025-05-24 14:26:44
 */
public interface PictureService extends IService<PictureInfo> {

    /**
     * 上传图片
     *
     * @param pictureInputSource   上传源 (文件 或者 地址)
     * @param pictureUploadRequest 上传请求
     * @return 图片信息
     */
    PictureDetailVO uploadPicture(Object pictureInputSource, PictureUploadRequest pictureUploadRequest);


    /**
     * 根据id删除图片
     *
     * @param pictureId 图片id
     */
    void deletePictureById(long pictureId);


    /**
     * 根据id修改图片信息
     *
     * @param pictureEditRequest 修改请求
     */
    void editPicture(PictureEditRequest pictureEditRequest);


    /**
     * 根据id获取图片详情
     *
     * @param pictureId 图片id
     * @return 图片详情
     */
    PictureDetailVO getPictureDetailById(Long pictureId);


    /**
     * 获取个人空间图片分页列表
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 个人空间图片分页列表
     */
    PageVO<PictureVO> getPicturePageListAsPersonSpace(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取团队空间图片分页列表
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 团队空间图片分页列表
     */
    PageVO<PictureVO> getPicturePageListAsTeamSpace(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取收藏图片分页列表
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 收藏图片分页列表
     */
    PageVO<PictureHomeVO> getCollectPictureList(PictureQueryRequest pictureQueryRequest);


    /**
     * 获取当前用户 收藏数量 和上传数量
     *
     * @return 用户收藏数量和上传数量
     */
    UserPictureStatsVO getUserPictureStats();

    /**
     * 下载图片
     *
     * @param pictureId 图片id
     * @return 图片下载地址
     */
    String pictureDownload(Long pictureId);

    /**
     * 填充审核参数
     *
     * @param pictureInfo 图片信息
     * @param userInfo    用户信息
     */
    void fillReviewParams(PictureInfo pictureInfo, UserInfo userInfo);

    /**
     * 进行点赞或者收藏
     *
     * @param pictureInteractionRequest 点赞或者收藏请求
     */
    void likeOrCollection(PictureInteractionRequest pictureInteractionRequest);

    /**
     * 判断图片是否存在
     *
     * @param pictureId 图片id
     */
    void existPictureById(Long pictureId);


    /**
     * 获取redis指定key的变化量
     *
     * @param redisKey
     * @param pictureIds
     * @return
     */
    Map<Long, Integer> getRedisDeltas(String redisKey, List<Long> pictureIds);

    /**
     * 爬取图片
     *
     * @param capturePictureRequest 爬取请求
     * @return 爬取数量
     */
    List<CapturePictureResult> capturePicture(capturePictureRequest capturePictureRequest);

    /**
     * 上传爬取的图片
     *
     * @param pictureUploadRequest 上传请求
     */

    void uploadPictureByCapture(PictureUploadRequest pictureUploadRequest);

    /**
     * 创建 AI扩图任务
     *
     * @param pictureExtendRequest 扩图任务请求
     * @return 扩图任务结果
     */
    CreateOutPaintingTaskResponse createPictureExtendTask(PictureExtendRequest pictureExtendRequest);

    /**
     * 获取 AI扩图任务
     *
     * @param taskId 任务id
     * @return 扩图结果
     */
    GetOutPaintingTaskResponse queryPictureExtendTask(String taskId);

    //-------------管理员使用-------------

    /**
     * 以图搜图
     *
     * @param searchPictureByPictureRequest 以图搜图请求
     * @return 搜索结果
     */
    List<SearchPictureResult> searchPictureByPicture(SearchPictureByPictureRequest searchPictureByPictureRequest);

    /**
     * 颜色搜索(目前仅 搜索自己的空间)
     *
     * @param searchPictureByColorRequest 颜色搜索请求
     * @return 搜索结果
     */
    PageVO<PictureVO> searchPictureByPicColor(SearchPictureByColorRequest searchPictureByColorRequest);

    /**
     * 审核图片 (包含批量审核)
     *
     * @param pictureReviewRequest 审核请求
     */
    void reviewPicture(PictureReviewRequest pictureReviewRequest);

    /**
     * 管理员更新图片
     *
     * @param pictureUpdateRequest 更新请求
     */
    void updatePicture(PictureUpdateRequest pictureUpdateRequest);

    /**
     * 分页获取图片列表 (仅管理员可用)
     *
     * @param pictureQueryRequest 请求参数
     * @return 图片列表
     */
    PageVO<PictureVO> getPicturePageListAsManage(PictureQueryRequest pictureQueryRequest);

    /**
     * 构造查询参数
     *
     * @param pictureQueryRequest 请求参数
     * @return LambdaQueryWrapper
     */
    @SneakyThrows(ParseException.class)
    LambdaQueryWrapper<PictureInfo> getLambdaQueryWrapper(PictureQueryRequest pictureQueryRequest);

}

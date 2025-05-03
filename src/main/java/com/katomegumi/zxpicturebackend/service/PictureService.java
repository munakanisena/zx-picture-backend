package com.katomegumi.zxpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.katomegumi.zxpicturebackend.core.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.katomegumi.zxpicturebackend.model.dto.picture.*;
import com.katomegumi.zxpicturebackend.model.dao.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.katomegumi.zxpicturebackend.model.dao.entity.User;
import com.katomegumi.zxpicturebackend.model.vo.PictureVO;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lirui
* @description 针对表【tb_picture(图片)】的数据库操作Service
* @createDate 2025-02-15 19:51:44
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 封装 query对象 便于读取
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取单个图片封装
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取多个图片 封装
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void deletePicture(long pictureId, User loginUser);

    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);


    /**
     * 图片更新 修改时进行校验
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 审核请求
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest,User loginUser);

    /**
     * 更新或者修改 都需要进行审核
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取图片
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    @Async
    void clearPictureFile(Picture picture);

    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 根据主色调查找相似图片
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureByPicColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批处理图片
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建扩图任务
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     * @return 任务ID
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}

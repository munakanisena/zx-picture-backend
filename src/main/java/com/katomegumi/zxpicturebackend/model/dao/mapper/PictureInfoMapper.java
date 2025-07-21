package com.katomegumi.zxpicturebackend.model.dao.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo;
import com.katomegumi.zxpicturebackend.model.vo.picture.PictureHomeVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【picture_info(图片信息表)】的数据库操作Mapper
 * @createDate 2025-05-24 14:26:44
 * @Entity com.katomegumi.zxpicturebackend.model.dao.entity.PictureInfo
 */
public interface PictureInfoMapper extends BaseMapper<PictureInfo> {

    /**
     * 查询图片列表(包括关联的图片用户) 已弃用 改为查询用户信息后 手动设置
     *
     * @param page               分页
     * @param lambdaQueryWrapper 查询条件
     * @return 图片列表
     */
    @Deprecated
    @Select("SELECT pi.*,ui.username,ui.user_avatar FROM picture_info pi LEFT JOIN user_info ui ON pi.user_id = ui.id" + " ${ew.customSqlSegment}")
    IPage<PictureHomeVO> selectPageWithUser(Page<?> page, @Param("ew") LambdaQueryWrapper<PictureInfo> lambdaQueryWrapper);


    /**
     * 批量更新点赞和收藏数 (+变化量)
     *
     * @param pictureInfoList 图片实体
     * @return
     */
    void batchIncrementLikesAndCollects(@Param("list") List<PictureInfo> pictureInfoList);
}





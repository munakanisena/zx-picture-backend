package com.katomegumi.zxpicturebackend.model.dao.mapper;

import com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Map;

/**
* @author lirui
* @description 针对表【user_info(用户信息表)】的数据库操作Mapper
* @createDate 2025-05-07 17:17:08
* @Entity com.katomegumi.zxpicturebackend.model.dao.entity.UserInfo
*/
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    /**
     * 根据 ID 集合查询用户，并以 ID 为键构建 Map
     *
     * @param userIds 用户 ID 集合
     * @return Map<用户ID, 用户实体>
     */
    @MapKey("id")
    Map<Long, UserInfo> selectMapByIds(@Param("userIds") Collection<Long> userIds);
}





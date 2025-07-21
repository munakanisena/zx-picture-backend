package com.katomegumi.zxpicturebackend.manager.auth;


import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.katomegumi.zxpicturebackend.manager.auth.model.SpaceUserRole;
import com.katomegumi.zxpicturebackend.service.SpaceUserService;
import com.katomegumi.zxpicturebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author Megumi
 * @description 获取对应的权限
 */
@Component
@RequiredArgsConstructor
public class SpaceUserAuthManager {


    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }


    /**
     * 根据角色获取权限列表
     *
     * @param role 用户角色
     * @return 权限列表
     */
    public List<String> getSpaceUserPermissionsByRole(String role) {
        if (role == null) {
            return Collections.emptyList();
        }
        SpaceUserRole spaceUserRole = SPACE_USER_AUTH_CONFIG
                .getRoles()
                .stream()
                .filter(role1 -> role1.getKey().equals(role))
                .findFirst()
                .orElse(null);
        if (spaceUserRole == null) {
            return Collections.emptyList();
        }
        return spaceUserRole.getPermissions();
    }


//    public List<String> getPermissionList(Space space, User loginUser) {
//        if (loginUser == null) {
//            return new ArrayList<>();
//        }
//        // 管理员权限
//        List<String> ADMIN_PERMISSIONS = getSpaceUserPermissionsByRole(SpaceRoleEnum.ADMIN.getKey());
//        // 公共图库
//        if (space == null) {
//            if (userServiceOld.isAdmin(loginUser)) {
//                return ADMIN_PERMISSIONS;
//            }
//            return new ArrayList<>();
//        }
//        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByKey(space.getSpaceType());
//        if (spaceTypeEnum == null) {
//            return new ArrayList<>();
//        }
//        // 根据空间获取对应的权限
//        switch (spaceTypeEnum) {
//            case PRIVATE:
//                // 私有空间，仅本人或管理员有所有权限
//                if (space.getUserId().equals(loginUser.getId()) || userServiceOld.isAdmin(loginUser)) {
//                    return ADMIN_PERMISSIONS;
//                } else {
//                    return new ArrayList<>();
//                }
//            case TEAM:
//                // 团队空间，查询 SpaceUserOld 并获取角色和权限
//                SpaceUserOld spaceUserOld = spaceUserServiceOld.lambdaQuery()
//                        .eq(SpaceUserOld::getSpaceId, space.getId())
//                        .eq(SpaceUserOld::getUserId, loginUser.getId())
//                        .one();
//                if (spaceUserOld == null) {
//                    return new ArrayList<>();
//                } else {
//                    return getSpaceUserPermissionsByRole(spaceUserOld.getSpaceRole());
//                }
//        }
//        return new ArrayList<>();
//    }

}

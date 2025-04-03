package com.katomegumi.zxpicturebackend.manager.auth;


import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.katomegumi.zxpicturebackend.manager.auth.model.SpaceUserRole;
import com.katomegumi.zxpicturebackend.model.entity.Space;
import com.katomegumi.zxpicturebackend.model.entity.SpaceUser;
import com.katomegumi.zxpicturebackend.model.entity.User;
import com.katomegumi.zxpicturebackend.model.enums.SpaceRoleEnum;
import com.katomegumi.zxpicturebackend.model.enums.SpaceTypeEnum;
import com.katomegumi.zxpicturebackend.service.SpaceService;
import com.katomegumi.zxpicturebackend.service.SpaceUserService;
import com.katomegumi.zxpicturebackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 空间成员管理权限
 */
@Component
public class SpaceUserAuthManager {


    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;
    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG=JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     * @param role
     * @return
     */
    public List<String> getSpaceUserPermissionsByRole(String role) {
        if (role == null) {
            return Collections.emptyList();
        }
        SpaceUserRole spaceUserRole = SPACE_USER_AUTH_CONFIG.getRoles()
                .stream()
                .filter(role1 -> role1.getKey().equals(role))
                .findFirst()
                .orElse(null); //没有就设置为null
        if (spaceUserRole == null) {
            return Collections.emptyList();
        }
        return spaceUserRole.getPermissions();
    }



    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getSpaceUserPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getSpaceUserPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }

}

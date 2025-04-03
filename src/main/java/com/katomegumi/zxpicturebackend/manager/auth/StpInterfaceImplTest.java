package com.katomegumi.zxpicturebackend.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.manager.auth.model.SpaceUserAuthContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author : 惠
 * @description :
 * @createDate : 2025/3/30 下午6:12
 */
@Component
public class StpInterfaceImplTest implements StpInterface {

    @Value("${server.servlet.context-path}")
    private String contextPat;

    @Resource
    private  SpaceUserAuthManager spaceUserAuthManager;

    //获取权限列表
    @Override
    public List<String> getPermissionList(Object o, String s) {
        return null;
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        return List.of();
    }

        //通过(请求)获取  上下文  需要的参数
    public SpaceUserAuthContext getSpaceUserAuthContextByRequest() {
        //获取请求对象
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String contentType  = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext spaceUserAuthContext ;
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            spaceUserAuthContext = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        }else {
            Map<String, String> map = ServletUtil.getParamMap(request);
            spaceUserAuthContext= BeanUtil.toBean(map, SpaceUserAuthContext.class);
        }
        //判断是哪个Id
        Long id= spaceUserAuthContext.getId();
        String requestURI = request.getRequestURI();
        String replaceUrl = requestURI.replace(contextPat+"/","");
        String subString =StrUtil.subBefore(replaceUrl,"/",false);
        if(subString!=null&&subString.length()>0){
            switch (subString){
                case "picture":
                    spaceUserAuthContext.setPictureId(id);
                    break;
                case "spaceUser":
                    spaceUserAuthContext.setSpaceUserId(id);
                    break;
                case "space":
                    spaceUserAuthContext.setSpaceId(id);
                    break;
                default:
            }
        }
        return spaceUserAuthContext;
    }
}


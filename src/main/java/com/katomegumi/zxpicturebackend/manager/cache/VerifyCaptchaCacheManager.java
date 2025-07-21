package com.katomegumi.zxpicturebackend.manager.cache;

import cn.hutool.core.util.ObjectUtil;
import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.constant.CacheConstant;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author : Megumi
 * @description : 验证码缓存管理类
 * @createDate : 2025/5/7 下午1:25
 */
@Component
@RequiredArgsConstructor
public class VerifyCaptchaCacheManager {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 验证码存入redis中 并且做限制
     * 1分钟只能发一条
     * @param captcha 验证码
     * @param to 收件人
     */
    public void putCaptchaIntoRedis(String captcha,String to){
        String key = CacheConstant.EMAIL.REGISTER + to;
        BoundHashOperations<String, String, String> boundHashOps = stringRedisTemplate.boundHashOps(key);
        //获取最后一次发送邮件的事件
        String lastSendTime = boundHashOps.get(CacheConstant.EMAIL.LAST_SEND_TIME);
        //获取一共发送多少邮件
        String sendCount = boundHashOps.get(CacheConstant.EMAIL.COUNT);

        if (StringUtils.isBlank(lastSendTime) && StringUtils.isBlank(sendCount)) {
            boundHashOps.put(CacheConstant.EMAIL.CAPTCHA, captcha);
            boundHashOps.put(CacheConstant.EMAIL.LAST_SEND_TIME, String.valueOf(System.currentTimeMillis()));
            boundHashOps.put(CacheConstant.EMAIL.COUNT, "1");
            boundHashOps.expire(5, TimeUnit.MINUTES);
            return;
        }
        if(StringUtils.isNotBlank(sendCount)&&Integer.parseInt(sendCount)>=5) {
            //覆盖设置 24小时后过期
            boundHashOps.expire(24, TimeUnit.HOURS);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码发送频繁,24小时重试");
        }
        if (StringUtils.isNotBlank(lastSendTime)){
            long elapsedTime = System.currentTimeMillis() - Long.parseLong(lastSendTime);
            if (elapsedTime < 1000 * 60) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码发送频繁,1分钟重试");
            }
        }
        boundHashOps.put(CacheConstant.EMAIL.CAPTCHA, captcha);
        boundHashOps.put(CacheConstant.EMAIL.LAST_SEND_TIME, String.valueOf(System.currentTimeMillis()));
        boundHashOps.put(CacheConstant.EMAIL.COUNT, String.valueOf(sendCount != null ? Integer.parseInt(sendCount) + 1 : "1"));
        //验证码5分钟过期
        boundHashOps.expire(5,TimeUnit.MINUTES);
    }

    /**
     * 插入数据
     */
    public void set(String key,String value,long expire,TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key,value,expire,timeUnit);
    }

    /**
     * 校验注册验证码
     * @param key
     * @param captcha
     * @return
     */
    public boolean verifyCaptchaOk(String key,String captcha) {
        return ObjectUtil.equals(stringRedisTemplate.opsForHash().get(key, CacheConstant.EMAIL.CAPTCHA), captcha);
    }

    /**
     * 校验重置验证码
     * @param key
     * @param captcha
     * @return
     */
    public boolean verifyForgotCaptchaOk(String key,String captcha) {
        return ObjectUtil.equals(stringRedisTemplate.opsForValue().get(key),captcha);
    }

    /**
     * 删除验证码
     * @param key
     */
    public void removeCaptcha(String key) {
      stringRedisTemplate.delete(key);
    }
}


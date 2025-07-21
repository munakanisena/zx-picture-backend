package com.katomegumi.zxpicturebackend.manager.email;

import com.katomegumi.zxpicturebackend.core.common.exception.BusinessException;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.constant.CacheConstant;
import com.katomegumi.zxpicturebackend.core.util.EmailUtils;
import com.katomegumi.zxpicturebackend.manager.cache.VerifyCaptchaCacheManager;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;

/**
 * @author : Megumi
 * @description : 邮件发送客户端
 * @createDate : 2025/5/3 下午7:37
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailManager {

    private final JavaMailSender javaMailSender;

    private final TemplateEngine  templateEngine;

    private final VerifyCaptchaCacheManager verifyCaptchaCacheManager;

    @Value("${verify.code.length}")
    //验证码长度
    private int length;
    //发件人名称
    @Value("${spring.mail.nickname}")
    private String nickname;
    //发件人地址
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮件注册验证码
     * @param to 收件人
     * todo 由于这里用的异步线程 所以这里抛出的异常controller捕获不到 暂时前端做校验
     */
    @Async(value = "emailThreadPool")
    public void sendEmailCaptcha(String to) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(nickname + "<" + from + ">");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject("惠眸图界 - 注册验证码");
            //获取验证码
            String randomCaptcha = EmailUtils.getRandomCaptcha(length);
            //将验证码存入redis
            verifyCaptchaCacheManager.putCaptchaIntoRedis(randomCaptcha, to);
            //获取邮件模版
            Context context = new Context();
            context.setVariable(CacheConstant.EMAIL.GET_CAPTCHA, randomCaptcha);
            String htmlContent = templateEngine.process("RegisterTemplate.html", context);
            mimeMessageHelper.setText(htmlContent,true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"邮件验证码发送失败");
        }
    }

    /**
     * 重置用户密码
     * @param to 收件人
     */
    @Async(value = "emailThreadPool")
    public void sendEmailForgotPassword(String to) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(nickname + "<" + from + ">");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject("惠眸图界 - 重置密码");
            //获取随机验证码
            String randomCaptcha = EmailUtils.getRandomCaptcha(length);
            //添加到redis 重置密码不做限制
            verifyCaptchaCacheManager.set(CacheConstant.EMAIL.FORGOT + to, randomCaptcha, 5, TimeUnit.MINUTES);
            //获取邮件模版
            Context context = new Context();
            context.setVariable(CacheConstant.EMAIL.GET_CAPTCHA, randomCaptcha);
            String htmlContent = templateEngine.process("ForgotPasswordTemplate.html", context);
            mimeMessageHelper.setText(htmlContent,true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"邮件验证码发送失败");
    }
    }
}


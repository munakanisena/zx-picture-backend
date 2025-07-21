package com.katomegumi.zxpicturebackend.core.config;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.katomegumi.zxpicturebackend.core.common.exception.ErrorCode;
import com.katomegumi.zxpicturebackend.core.common.util.ResultUtils;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * @author Megumi
 * @description 限流过滤器
 * @createDate 2025/6/23 16:47
 */
@Component
public class Bucket4jRateLimitingFilter implements Filter {

    @Value("${Bucket4j.CAPACITY}")
    private static final int CAPACITY = 50;

    @Value("${Bucket4j.REFILL_TOKENS}")
    private static final int REFILL_TOKENS = 10;

    private final Bucket bucket;

    public Bucket4jRateLimitingFilter() {
        this.bucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(CAPACITY).refillGreedy(REFILL_TOKENS, Duration.ofSeconds(1)))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.HTTP_OK);
            httpResponse.setContentType(ContentType.JSON.toString(StandardCharsets.UTF_8));
            httpResponse.getWriter().write(JSONUtil.toJsonStr(ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务器繁忙,请稍后再试")));
            httpResponse.getWriter().flush();
        }
    }

}
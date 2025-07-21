package com.katomegumi.zxpicturebackend.core.config;

import lombok.Data;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * @author : Megumi
 * @description : OkHttpClient 配置
 * @createDate : 2025/5/29 下午12:25
 */
@ConfigurationProperties(prefix = "okhttp")
@Configuration
@Data
public class OkHttpClientConfig {
    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer writeTimeout;
    private Integer maxIdleConnections;
    private Long keepAliveDuration;

    @Bean
    public OkHttpClient okHttpClient() {
        //后续考虑增加代理
        return new OkHttpClient.Builder()
                .sslSocketFactory(this.sslSocketFactory(), this.x509TrustManager())
                //信任所有主机名
                .hostnameVerifier((var1, var2) -> true)
                //连接超时
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                //读取超时
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                //写入超时
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                //连接池
                .connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MINUTES))
                //失败重试
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * 信任所有证书
     *
     * @return 返回证书管理类
     */
    @Bean
    public X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    /**
     * 套接字工厂
     *
     * @return 返回套接字工厂
     */
    @Bean
    public SSLSocketFactory sslSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{this.x509TrustManager()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }

}


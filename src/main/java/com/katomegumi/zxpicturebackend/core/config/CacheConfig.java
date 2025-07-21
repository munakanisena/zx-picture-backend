package com.katomegumi.zxpicturebackend.core.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.katomegumi.zxpicturebackend.core.constant.CacheConstant;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


/**
 * @author : Megumi
 * @description : 缓存配置类 多级缓存
 * @createDate : 2025/5/27 下午9:08
 */
@Configuration
public class CacheConfig {
    // Caffeine 缓存管理器 - 用于图片分类 (这里需要指定默认的管理器)
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                //初始化缓存目数
                .initialCapacity(20)
                // 最大缓存条目数
                .maximumSize(1000L)
                // 写入后30分钟过期
                .expireAfterWrite(30, TimeUnit.MINUTES)
                // 启用统计
                .recordStats());
        return cacheManager;
    }

    // Redis 缓存管理器 - 用于图片列表
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 默认5分钟过期 刷新
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // JSON序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .prefixCacheNameWith(CacheConstant.REDIS_CACHE_PREFIX);

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();

        redisCacheManager.setTransactionAware(true);
        redisCacheManager.initializeCaches();

        return redisCacheManager;
    }
}


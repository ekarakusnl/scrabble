package com.gamecity.scrabble.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

/**
 * Spring configuration for Redis cache
 * 
 * @author ekarakus
 */
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    @Bean
    RedisCacheManager cacheManager(JedisConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }

}

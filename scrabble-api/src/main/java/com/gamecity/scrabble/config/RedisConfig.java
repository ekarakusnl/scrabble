package com.gamecity.scrabble.config;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring configuration for Redis including push messages and cache connection
 * 
 * @author ekarakus
 */
@Configuration
@EnableCaching
@PropertySource("classpath:redis.properties")
@Slf4j
public class RedisConfig extends CachingConfigurerSupport {

    private static final Integer TTL_SECONDS = 3600;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private Integer redisPort;

    @Value("${redis.username}")
    private String redisUsername;

    @Value("${redis.password}")
    private String redisPassword;

    @Value("${redis.use.ssl}")
    private boolean useSsl;

    @Bean
    JedisConnectionFactory connectionFactory() {
        log.debug("Connecting to '{}:{}'", redisHost, redisPort);
        final RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (StringUtils.isNotEmpty(redisUsername) && StringUtils.isNotEmpty(redisPassword)) {
            redisConfiguration.setUsername(redisUsername);
            redisConfiguration.setPassword(RedisPassword.of(redisPassword));
        }

        final JedisClientConfigurationBuilder jedisConfigurationBuilder = JedisClientConfiguration.builder();
        jedisConfigurationBuilder.connectTimeout(Duration.ofSeconds(60));
        jedisConfigurationBuilder.readTimeout(Duration.ofSeconds(60));
        if (useSsl) {
            log.debug("Redis server is ssl secured");
            jedisConfigurationBuilder.useSsl();
        }
        jedisConfigurationBuilder.usePooling();

        final JedisClientConfiguration jedisClientConfiguration = jedisConfigurationBuilder.build();

        return new JedisConnectionFactory(redisConfiguration, jedisClientConfiguration);
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    RedisCacheManager cacheManager(JedisConnectionFactory connectionFactory) {
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .disableCachingNullValues()
                        .computePrefixWith(name -> name + ":")
                        .serializeValuesWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                        .entryTtl(Duration.ofSeconds(TTL_SECONDS)))
                .build();
        return redisCacheManager;
    }

}

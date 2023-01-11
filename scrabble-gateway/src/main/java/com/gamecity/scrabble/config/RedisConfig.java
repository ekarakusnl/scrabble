package com.gamecity.scrabble.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.gamecity.scrabble.controller.ActionController;
import com.gamecity.scrabble.controller.ChatController;

/**
 * Spring configuration for Redis
 * 
 * @author ekarakus
 */
@SuppressWarnings("rawtypes")
@Configuration
@PropertySource("classpath:redis.properties")
public class RedisConfig extends CachingConfigurerSupport {

    private static final String ACTION = "ACTION";
    private static final String CHATS = "CHATS";

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private Integer redisPort;

    @Value("${redis.username}")
    private String redisUsername;

    @Value("${redis.password}")
    private String redisPassword;

    @Value("${redis.ssl}")
    private boolean useSsl;

    @Autowired
    private ActionController actionController;

    @Autowired
    private ChatController chatController;

    @Bean
    JedisConnectionFactory connectionFactory() {
        final RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisUsername != null && redisPassword != null) {
            redisConfiguration.setUsername(redisUsername);
            redisConfiguration.setPassword(RedisPassword.of(redisPassword));
        }

        final JedisClientConfigurationBuilder jedisConfigurationBuilder = JedisClientConfiguration.builder();
        jedisConfigurationBuilder.connectTimeout(Duration.ofSeconds(60));
        jedisConfigurationBuilder.readTimeout(Duration.ofSeconds(60));
        if (useSsl) {
            jedisConfigurationBuilder.useSsl();
        }
        jedisConfigurationBuilder.usePooling();

        final JedisClientConfiguration jedisClientConfiguration = jedisConfigurationBuilder.build();

        return new JedisConnectionFactory(redisConfiguration, jedisClientConfiguration);
    }

    @Bean
    RedisTemplate redisTemplate(JedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(JedisConnectionFactory connectionFactory) {
        final RedisMessageListenerContainer messageListenerContainer = new RedisMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.addMessageListener(actionController, new PatternTopic(ACTION));
        messageListenerContainer.addMessageListener(chatController, new PatternTopic(CHATS));
        return messageListenerContainer;
    }

}

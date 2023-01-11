package com.gamecity.scrabble.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

import com.gamecity.scrabble.controller.ActionController;
import com.gamecity.scrabble.controller.ChatController;

/**
 * Spring configuration for Redis
 * 
 * @author ekarakus
 */
@SuppressWarnings("rawtypes")
@Configuration
@EnableRedisHttpSession
@PropertySource("classpath:redis.properties")
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {

    private static final String ACTION = "ACTION";
    private static final String CHATS = "CHATS";

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private Integer redisPort;

    @Autowired
    private ActionController actionController;

    @Autowired
    private ChatController chatController;

    @Bean
    JedisConnectionFactory connectionFactory() {
        final RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new JedisConnectionFactory(redisConfiguration);
    }

    @Bean
    RedisTemplate redisTemplate(JedisConnectionFactory connectionFactory) {
        final RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
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

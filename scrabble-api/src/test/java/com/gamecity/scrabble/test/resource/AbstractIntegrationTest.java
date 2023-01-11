package com.gamecity.scrabble.test.resource;

import java.io.IOException;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import com.gamecity.scrabble.config.AspectConfig;
import com.gamecity.scrabble.config.RedisConfig;
import com.gamecity.scrabble.resource.impl.ResourceExceptionMapper;

import redis.embedded.RedisServer;

import com.gamecity.scrabble.config.PersistenceConfig;
import com.gamecity.scrabble.config.PropertyConfig;

abstract class AbstractIntegrationTest extends JerseyTest {

    private static RedisServer redisServer;
    private static ResourceConfig resourceConfig;
    private static AnnotationConfigApplicationContext applicationContext;
    protected static RedisTemplate<String, Object> redisTemplate;

    @BeforeAll
    public static void beforeAll() throws IOException {
        redisServer = new RedisServer(6380);
        redisServer.start();
        initializeApplicationContext();
    }

    @AfterAll
    public static void afterAll() {
        redisServer.stop();
        applicationContext.close();
        resourceConfig = null;
    }

    private static void initializeApplicationContext() {
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.scan("com.gamecity.scrabble.dao", "com.gamecity.scrabble.service",
                "com.gamecity.scrabble.resource");
        applicationContext.register(PersistenceConfig.class, RedisConfig.class, AspectConfig.class,
                PropertyConfig.class);
        applicationContext.refresh();

        redisTemplate = (RedisTemplate<String, Object>) applicationContext.getBean("redisTemplate");
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected Application configure() {
        if (resourceConfig != null) {
            return resourceConfig;
        }

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        resourceConfig = new ResourceConfig();
        resourceConfig.property("contextConfig", applicationContext);
        resourceConfig.register(applicationContext.getBean("bagResource"));
        resourceConfig.register(applicationContext.getBean("boardResource"));
        resourceConfig.register(applicationContext.getBean("chatResource"));
        resourceConfig.register(applicationContext.getBean("gameResource"));
        resourceConfig.register(applicationContext.getBean("playerResource"));
        resourceConfig.register(applicationContext.getBean("userResource"));
        resourceConfig.register(applicationContext.getBean("virtualBoardResource"));
        resourceConfig.register(applicationContext.getBean("virtualRackResource"));
        resourceConfig.register(applicationContext.getBean("wordResource"));
        resourceConfig.register(ResourceExceptionMapper.class);

        return resourceConfig;
    }

}

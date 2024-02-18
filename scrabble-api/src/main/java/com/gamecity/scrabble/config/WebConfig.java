package com.gamecity.scrabble.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.Generated;

/**
 * Spring configuration of Web MVC
 * 
 * @author ekarakus
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
        "com.gamecity.scrabble.dao", "com.gamecity.scrabble.service", "com.gamecity.scrabble.resource",
        "com.gamecity.scrabble.job", "com.gamecity.scrabble.aspect"
}, basePackageClasses = {
        PersistenceConfig.class, AspectConfig.class, PropertyConfig.class
})
@Generated
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

}

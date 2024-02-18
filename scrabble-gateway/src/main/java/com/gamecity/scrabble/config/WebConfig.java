package com.gamecity.scrabble.config;

import jakarta.servlet.MultipartConfigElement;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.common.net.HttpHeaders;

/**
 * Spring configuration for Web MVC
 * 
 * @author ekarakus
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.gamecity.scrabble")
@PropertySource("classpath:application.properties")
public class WebConfig implements WebMvcConfigurer {

    private static final long ASYNCHRONOUS_REQUEST_DURATION = 30 * 1000L;
    private static final long MAX_FILE_UPLOAD_SIZE = 10 * 1024 * 1024;
    private static final String FILE_UPLOAD_DIRECTORY = "/tmp";

    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean(name = "multipartResolver")
    StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement(FILE_UPLOAD_DIRECTORY, MAX_FILE_UPLOAD_SIZE, -1, 0);
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(ASYNCHRONOUS_REQUEST_DURATION);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/rest/**")
                .allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name())
                .exposedHeaders(HttpHeaders.AUTHORIZATION);
        registry.addMapping("/login/**").allowedMethods(HttpMethod.POST.name());
    }

}

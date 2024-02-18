package com.gamecity.scrabble.config;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.gamecity.scrabble.filter.CorsFilter;
import com.gamecity.scrabble.servlet.JerseyServlet;

import lombok.Generated;

/**
 * Main initializer to setup application configuration
 * 
 * @author ekarakus
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Generated
public class ServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);
        // since jersey-spring registers its own contextConfigLocation, this one is disabled to
        // prevent any conflicts during contextConfig initialization
        servletContext.setInitParameter("contextConfigLocation", "<NONE>");
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { WebConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { JerseyServlet.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[] { new OpenEntityManagerInViewFilter(), new CorsFilter() };
    }

}

package com.gamecity.scrabble.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;

import com.google.common.net.HttpHeaders;

/**
 * {@link Filter} for CORS operations
 * 
 * @author ekarakus
 */
public class CorsFilter implements Filter {

    private static final String[] ALLOWED_METHODS = new String[] { //
            HttpMethod.POST.name(), //
            HttpMethod.GET.name(), //
            HttpMethod.PUT.name(), //
            HttpMethod.OPTIONS.name(), //
            HttpMethod.DELETE.name() //
    };

    private static final String[] ALLOWED_HEADERS = new String[] { //
            HttpHeaders.CONTENT_TYPE, //
            HttpHeaders.AUTHORIZATION, //
            HttpHeaders.ORIGIN, //
            HttpHeaders.ACCEPT, //
            HttpHeaders.X_REQUESTED_WITH //
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(",", ALLOWED_METHODS));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(",", ALLOWED_HEADERS));
        chain.doFilter(req, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

}

package com.gamecity.scrabble.filter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gamecity.scrabble.service.impl.JwtProvider;
import com.google.common.net.HttpHeaders;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link OncePerRequestFilter} for JWT authentication
 * 
 * @author ekarakus
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PARAMETER = "HTTP_TOKEN ";

    @Autowired
    private JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(TOKEN_PARAMETER)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = StringUtils.substringAfter(header, TOKEN_PARAMETER);

        try {
            jwtProvider.validateToken(token);
        } catch (RuntimeException e) {
            log.error("An error occured while validating the token", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(request, response);
            return;
        }

        final SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext.getAuthentication() != null) {
            filterChain.doFilter(request, response);
            SecurityContextHolder.getContext().setAuthentication(securityContext.getAuthentication());
            return;
        }

        final UsernamePasswordAuthenticationToken authentication;
        try {
            authentication = jwtProvider.getAuthenticationToken(token);
        } catch (ExecutionException e) {
            log.error("An error occured while getting the user", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(request, response);
            return;
        }

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

}

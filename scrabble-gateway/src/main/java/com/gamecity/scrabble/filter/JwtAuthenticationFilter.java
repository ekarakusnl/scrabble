package com.gamecity.scrabble.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gamecity.scrabble.api.model.BaseAuthority;
import com.gamecity.scrabble.api.model.User;
import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.service.RestService;
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

    private static final String TOKEN_PARAMETER = "HTTP_TOKEN";

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private RestService restService;

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

        final String username = jwtProvider.getUsername(token);

        final UserDto userDto = restService.get("/users/by/{username}", UserDto.class, username);
        // UserDetails userDetails = userAuthService.loadUserByUsername(userName);

        final Collection<GrantedAuthority> authorities = userDto.getAuthorities()
                .stream()
                .map(authority -> new BaseAuthority(authority))
                .collect(Collectors.toList());

        final User user = new User(userDto.getId(), userDto.getUsername(), userDto.getPassword(), userDto.isEnabled(),
                userDto.isAccountNonExpired(), userDto.isAccountNonLocked(), userDto.isCredentialsNonExpired(),
                authorities);

        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

}

package com.gamecity.scrabble.service.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.gamecity.scrabble.api.model.BaseAuthority;
import com.gamecity.scrabble.api.model.User;
import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.service.RestService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides methods for JWT operations
 * 
 * @author ekarakus
 */
@Component(value = "jwtProvider")
@Slf4j
public class JwtProvider {

    private Cache<String, UserDto> userCache;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.token.validity.hours}")
    private int tokenValidityHours;

    private RestService restService;

    @Autowired
    void setRestService(RestService restService) {
        this.restService = restService;
    }

    @PostConstruct
    void init() {
        // store the authenticated users in cache until the validity of tokens expire
        this.userCache = CacheBuilder.newBuilder().expireAfterWrite(tokenValidityHours, TimeUnit.HOURS).build();
    }

    /**
     * Gets the username stored by JWT
     * 
     * @param token the user token
     * @return the username
     */
    public String getUsername(final String token) {
        final Claims body = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return body.getSubject();
    }

    /**
     * Generates a JWT token
     * 
     * @param authentication the authentication object
     * @return the generated token
     */
    public String generateToken(Authentication authentication) {
        final User user = (User) authentication.getPrincipal();
        Claims claims = Jwts.claims().setSubject(user.getUsername());

        final Date now = new Date();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, tokenValidityHours);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(calendar.getTime())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Validates a JWT token
     * 
     * @param token the token
     */
    public void validateToken(final String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
        } catch (SignatureException e) {
            log.error("Invalid JWT signature", e);
            throw new RuntimeException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token", e);
            throw new RuntimeException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token", e);
            throw new RuntimeException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token", e);
            throw new RuntimeException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty", e);
            throw new RuntimeException("JWT claims string is empty");
        }
    }

    /**
     * Gets the {@link UserDto user} for the specified token. If the user is not in the cache with the
     * specified token, then the user is fetched and added to the cache
     * 
     * @param token
     * @return the userPasswordAuthenticationToken
     * @throws ExecutionException
     */
    public UsernamePasswordAuthenticationToken getAuthenticationToken(String token) throws ExecutionException {
        final String username = getUsername(token);

        final UserDto userDto =
                userCache.get(token, () -> restService.get("/users/by/{username}", UserDto.class, username));

        final Collection<BaseAuthority> authorities = userDto.getAuthorities()
                .stream()
                .map(authority -> new BaseAuthority(authority))
                .collect(Collectors.toList());

        final User user = new User(userDto.getId(), userDto.getUsername(), userDto.getPassword(),
                userDto.getPreferredLanguage(), userDto.isEnabled(), userDto.isAccountNonExpired(),
                userDto.isAccountNonLocked(), userDto.isCredentialsNonExpired(), authorities);

        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

}

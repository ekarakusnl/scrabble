package com.gamecity.scrabble.service.impl;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.gamecity.scrabble.api.model.User;

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

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.token.validity.hours}")
    private int tokenValidityHours;

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
            log.debug("Invalid JWT signature", e);
            throw new RuntimeException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            log.debug("Invalid JWT token", e);
            throw new RuntimeException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token", e);
            throw new RuntimeException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.debug("Unsupported JWT token", e);
            throw new RuntimeException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty", e);
            throw new RuntimeException("JWT claims string is empty");
        }
    }

}

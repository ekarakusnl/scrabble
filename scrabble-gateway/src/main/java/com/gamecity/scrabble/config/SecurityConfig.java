package com.gamecity.scrabble.config;

import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.gamecity.scrabble.filter.JwtAuthenticationFilter;
import com.gamecity.scrabble.jwt.JwtAuthenticationEntryPoint;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring configuration for application security
 * 
 * @author ekarakus
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter authenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((auth) ->
                auth.dispatcherTypeMatchers(DispatcherType.ASYNC)
                        .permitAll()
                    .requestMatchers("/rest/**")
                        .authenticated()
                    .requestMatchers("/login/**")
                        .permitAll()
                    .requestMatchers("/signup/**")
                        .permitAll()
                    
            )
            .formLogin((formLogin) -> formLogin.disable())
            .httpBasic((httpBasic) -> httpBasic.disable())
            .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint))
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(withDefaults())
            .csrf((csrf) -> csrf.disable());

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}

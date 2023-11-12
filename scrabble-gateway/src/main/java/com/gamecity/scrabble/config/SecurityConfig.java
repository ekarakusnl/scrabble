package com.gamecity.scrabble.config;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.gamecity.scrabble.api.model.BaseAuthority;
import com.gamecity.scrabble.api.model.User;
import com.gamecity.scrabble.filter.JwtAuthenticationFilter;
import com.gamecity.scrabble.jwt.JwtAuthenticationEntryPoint;
import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.service.RestService;

/**
 * Spring configuration for application security
 * 
 * @author ekarakus
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private RestService restService;

    @Autowired
    private JwtAuthenticationFilter authenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> {
            final UserDto userDto = restService.get("/users/by/{username}", UserDto.class, username);
            final Collection<GrantedAuthority> authorities = userDto.getAuthorities()
                    .stream()
                    .map(authority -> new BaseAuthority(authority))
                    .collect(Collectors.toList());

            return new User(userDto.getId(), userDto.getUsername(), userDto.getPassword(),
                    userDto.getPreferredLanguage(), userDto.isEnabled(), userDto.isAccountNonExpired(),
                    userDto.isAccountNonLocked(), userDto.isCredentialsNonExpired(), authorities);
        });
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/rest/**")
                .authenticated()
            .antMatchers("/login/**")
                .permitAll()
            .antMatchers("/signup/**")
                .permitAll()
            .and()
                .formLogin()
                    .disable()
                .httpBasic()
                    .disable()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
            .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .cors()
            .and()
                .csrf()
                    .disable();

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

}

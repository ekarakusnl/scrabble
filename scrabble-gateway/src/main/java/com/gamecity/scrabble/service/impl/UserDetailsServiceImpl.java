package com.gamecity.scrabble.service.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.api.model.BaseAuthority;
import com.gamecity.scrabble.api.model.User;
import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.service.RestService;

@Service("userDetailsService")
class UserDetailsServiceImpl implements UserDetailsService {

    private RestService restService;

    UserDetailsServiceImpl(final RestService restService) {
        this.restService = restService;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        final UserDto userDto = restService.get("/users/by/{username}", UserDto.class, username);
        final Collection<GrantedAuthority> authorities = userDto.getAuthorities()
                .stream()
                .map(authority -> new BaseAuthority(authority))
                .collect(Collectors.toList());

        return new User(userDto.getId(), userDto.getUsername(), userDto.getPassword(), userDto.getPreferredLanguage(),
                userDto.isEnabled(), userDto.isAccountNonExpired(), userDto.isAccountNonLocked(),
                userDto.isCredentialsNonExpired(), authorities);
    }

}

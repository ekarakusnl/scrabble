package com.gamecity.scrabble.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.api.model.User;
import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.model.rest.UserTokenDto;
import com.gamecity.scrabble.service.impl.JwtProvider;

/**
 * Login resources
 * 
 * @author ekarakus
 */
@CrossOrigin(allowCredentials = "true", origins = "http://web.gamecity.io")
@RestController
public class LoginController extends AbstractController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;

    /**
     * Logs a {@link UserDto user} in
     * 
     * @param userDto user to login
     * @return the created user token
     */
    @PostMapping("/login")
    public ResponseEntity<UserTokenDto> login(@RequestBody UserDto userDto) {
        final Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword()));

        final User user = (User) authentication.getPrincipal();

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final Set<String> roles = user.getAuthorities().stream().map(r -> r.getAuthority()).collect(Collectors.toSet());

        final String jwtToken = jwtProvider.generateToken(authentication);

        final UserTokenDto userTokenDto = new UserTokenDto();
        userTokenDto.setId(user.getId());
        userTokenDto.setToken(jwtToken);
        userTokenDto.setRoles(roles);

        return new ResponseEntity<>(userTokenDto, HttpStatus.OK);
    }
}

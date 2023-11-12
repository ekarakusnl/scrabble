package com.gamecity.scrabble.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.model.rest.UserProfileDto;

/**
 * {@link UserProfileDto User profile} resources
 * 
 * @author ekarakus
 */
@RestController
@RequestMapping(value = "/rest/users")
public class UserController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/users";

    /**
     * Gets the {@link UserProfileDto user}
     * 
     * @return the user
     */
    @GetMapping("/authenticated")
    public ResponseEntity<UserDto> getUser() {
        final UserDto userDto = get(API_RESOURCE_PATH + "/{userId}", UserDto.class, getUserId());
        final UserDto updateableUserDto = UserDto.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .preferredLanguage(userDto.getPreferredLanguage())
                .build();
        return new ResponseEntity<>(updateableUserDto, HttpStatus.OK);
    }

    /**
     * Save a {@link UserProfileDto user}
     * 
     * @param userDto dto to save
     * @return the saved dto
     */
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserDto userDto) {
        userDto.setId(getUserId());
        put(API_RESOURCE_PATH, UserDto.class, userDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

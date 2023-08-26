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
public class UserProfileController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/users";

    /**
     * Gets the {@link UserProfileDto user}
     * 
     * @return the user
     */
    @GetMapping("/current")
    public ResponseEntity<UserProfileDto> get() {
        final UserProfileDto userDto = get(API_RESOURCE_PATH + "/{userId}", UserProfileDto.class, getUserId());
        // do not return the actual password
        userDto.setPassword(null);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    /**
     * Save a {@link UserProfileDto user}
     * 
     * @param userProfileDto dto to save
     * @return the saved dto
     */
    @PutMapping
    public ResponseEntity<UserProfileDto> save(@RequestBody UserProfileDto userProfileDto) {
        final UserDto userDto = new UserDto();
        userDto.setId(getUserId());
        userDto.setEmail(userProfileDto.getEmail());
        userDto.setPassword(userProfileDto.getPassword());
        userDto.setPreferredLanguage(userProfileDto.getPreferredLanguage());
        userDto.setUsername(userProfileDto.getUsername());

        put(API_RESOURCE_PATH, UserDto.class, userDto);
        return get();
    }

}

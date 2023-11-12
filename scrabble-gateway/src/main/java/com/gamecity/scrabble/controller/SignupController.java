package com.gamecity.scrabble.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.model.rest.UserDto;

/**
 * Signup resources
 * 
 * @author ekarakus
 */
@RestController
public class SignupController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/users";

    /**
     * Create a {@link UserDto user}
     * @param userDto dto to save
     * @return the saved dto
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDto userDto) {
        put(API_RESOURCE_PATH, UserDto.class, userDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

package com.gamecity.scrabble.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.api.model.User;
import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.VirtualRackDto;

/**
 * {@link GameDto Game} resources
 * 
 * @author ekarakus
 */
@RestController
@RequestMapping(value = "/rest/games")
public class GameController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/games";

    /**
     * Gets a {@link GameDto game} by id
     * 
     * @param id <code>id</code> of the game
     * @return the game
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        final GameDto gameDto = get(API_RESOURCE_PATH + "/{gameId}", GameDto.class, id);
        return new ResponseEntity<>(gameDto, HttpStatus.OK);
    }

    /**
     * Gets the {@link List list} of {@link GameDto games}
     * 
     * @return the game list
     */
    @GetMapping
    public ResponseEntity<List<GameDto>> search() {
        final List<GameDto> list = list(API_RESOURCE_PATH + "?userId={userId}", GameDto.class, getUserId());
        if (list.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    /**
     * Creates a {@link GameDto game}
     * 
     * @param gameDto dto to create
     * @return the saved dto
     */
    @PutMapping
    public ResponseEntity<GameDto> create(@RequestBody GameDto gameDto) {
        gameDto.setOwnerId(getUserId());

        final GameDto responseGameDto = put(API_RESOURCE_PATH, GameDto.class, gameDto);
        return new ResponseEntity<>(responseGameDto, HttpStatus.OK);
    }

    /**
     * Joins the {@link GameDto game}
     * 
     * @param id <code>id</code> of the game
     * @return the updated dto
     */
    @PostMapping("/{id}/join")
    @ResponseBody
    public ResponseEntity<?> join(@PathVariable Long id) {
        put(API_RESOURCE_PATH + "/{id}/users/{userId}", GameDto.class, null, id, getUserId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Leaves the {@link GameDto game}
     * 
     * @param id <code>id</code> of the game
     * @return success
     */
    @PostMapping("/{id}/leave")
    @ResponseBody
    public ResponseEntity<?> leave(@PathVariable Long id) {
        delete(API_RESOURCE_PATH + "/{id}/users/{userId}", null, id, getUserId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Plays in the {@link GameDto game}
     * 
     * @param id   <code>id</code> of the game
     * @param rack rack of the user
     * @return success
     */
    @PostMapping("/{id}/play")
    @ResponseBody
    public ResponseEntity<?> play(@PathVariable Long id, @RequestBody VirtualRackDto rack) {
        post(API_RESOURCE_PATH + "/{id}/users/{userId}/rack", VirtualRackDto.class, rack, id, getUserId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets the {@link List list} of {@link GameDto games} by authenticated {@link User user}
     * 
     * @return the game list
     */
    @GetMapping("/by/user")
    public ResponseEntity<List<GameDto>> searchByUser() {
        final List<GameDto> list = list(API_RESOURCE_PATH + "?userId={userId}&includeUser=true", GameDto.class, getUserId());
        if (list.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

}

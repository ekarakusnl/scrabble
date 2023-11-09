package com.gamecity.scrabble.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.model.rest.PlayerDto;

/**
 * {@link PlayerDto Player} resources
 * 
 * @author ekarakus
 */
@RestController
@RequestMapping("/rest/games/{gameId}/players")
public class PlayerController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/games/{gameId}/players";

    /**
     * Gets the {@link List list} of {@link PlayerDto players}
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the player list
     * @return the player list
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<PlayerDto>> getPlayers(@PathVariable Long gameId, @RequestParam Integer version) {
        final List<PlayerDto> players =
                list(API_RESOURCE_PATH + "?version={version}", PlayerDto.class, gameId, version);
        return new ResponseEntity<>(players, HttpStatus.OK);
    }

    /**
     * Gets the {@link PlayerDto player} by <code>userId</code>
     * 
     * @param gameId <code>id</code> of the game
     * @return the player list
     */
    @GetMapping("/by/user")
    @ResponseBody
    public ResponseEntity<PlayerDto> getPlayer(@PathVariable Long gameId) {
        final PlayerDto player = get(API_RESOURCE_PATH + "/{userId}", PlayerDto.class, gameId, getUserId());
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

}

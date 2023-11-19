package com.gamecity.scrabble.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.model.rest.VirtualRackDto;

/**
 * {@link VirtualRackDto} resources
 * 
 * @author ekarakus
 */
@RestController
@RequestMapping("/rest/games/{gameId}/racks")
public class VirtualRackController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/games/{gameId}/racks";

    /**
     * Gets the {@link VirtualRackDto rack} by game id and round number
     * 
     * @param gameId      <code>id</code> of the game
     * @param roundNumber <code>number</code> of the round
     * @return the rack
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<VirtualRackDto> getRack(@PathVariable Long gameId, @RequestParam Integer roundNumber) {
        final VirtualRackDto rack = get(API_RESOURCE_PATH + "/users/{userId}?roundNumber={roundNumber}",
                VirtualRackDto.class, gameId, getUserId(), roundNumber);
        return new ResponseEntity<>(rack, HttpStatus.OK);
    }

}

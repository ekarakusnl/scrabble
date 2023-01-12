package com.gamecity.scrabble.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.model.rest.VirtualBoardDto;

/**
 * {@link VirtualBoardDto Board} resources
 * 
 * @author ekarakus
 */
@RestController
@RequestMapping("/rest/games/{gameId}/boards")
public class VirtualBoardController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/games/{gameId}/boards";

    /**
     * Gets the {@link VirtualBoardDto board} by game id and action counter
     * 
     * @param gameId        <code>id</code> of the game
     * @param actionCounter action counter
     * @return the virtual board
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<VirtualBoardDto> getBoard(@PathVariable Long gameId, @RequestParam Integer actionCounter) {
        final VirtualBoardDto boardDto =
                get(API_RESOURCE_PATH + "?actionCounter={actionCounter}", VirtualBoardDto.class, gameId, actionCounter);
        return new ResponseEntity<>(boardDto, HttpStatus.OK);
    }

}

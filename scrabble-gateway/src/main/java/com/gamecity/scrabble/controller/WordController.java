package com.gamecity.scrabble.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.model.rest.WordDto;

/**
 * {@link WordDto Word} resources
 * 
 * @author ekarakus
 */
@CrossOrigin(allowCredentials = "true", origins = "http://web.gamecity.io")
@RestController
@RequestMapping("/rest/games/{gameId}/words")
public class WordController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/games/{gameId}/words";

    /**
     * Gets the {@link List list} of {@link WordDto words}
     * 
     * @param gameId <code>id</code> of the game
     * @return the word list
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<WordDto>> getWords(@PathVariable Long gameId) {
        final List<WordDto> words = list(API_RESOURCE_PATH, WordDto.class, gameId);
        return new ResponseEntity<>(words, HttpStatus.OK);
    }

}

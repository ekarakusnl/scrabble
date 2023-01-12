package com.gamecity.scrabble.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.model.rest.BoardDto;

/**
 * {@link BoardDto Board} resources
 * 
 * @author ekarakus
 */
@RestController
@RequestMapping(value = "/rest/boards")
public class BoardController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/boards";

    /**
     * Gets a {@link BoardDto board} by id
     * 
     * @param id <code>id</code> of the board
     * @return the board
     */
    @GetMapping("/{id}")
    public ResponseEntity<BoardDto> get(@PathVariable Long id) {
        final BoardDto boardDto = get(API_RESOURCE_PATH + "/{id}", BoardDto.class, id);
        return new ResponseEntity<>(boardDto, HttpStatus.OK);
    }

    /**
     * Gets the {@link List list} of {@link BoardDto boards}
     * 
     * @return the board list
     */
    @GetMapping
    public ResponseEntity<List<BoardDto>> list() {
        final List<BoardDto> rules = list(API_RESOURCE_PATH, BoardDto.class);
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }

}

package com.gamecity.scrabble.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamecity.scrabble.model.rest.BagDto;

/**
 * {@link BagDto Bag} resources
 * 
 * @author ekarakus
 */
@CrossOrigin(allowCredentials = "true", origins = "http://web.gamecity.io")
@RestController
@RequestMapping(value = "/rest/bags")
public class BagController extends AbstractController {

    private static final String API_RESOURCE_PATH = "/bags";

    /**
     * Gets a {@link BagDto bag} by id
     * 
     * @param id <code>id</code> of the bag
     * @return the bag
     */
    @GetMapping("/{id}")
    public ResponseEntity<BagDto> get(@PathVariable Long id) {
        final BagDto bagDto = get(API_RESOURCE_PATH + "/{id}", BagDto.class, id);
        return new ResponseEntity<>(bagDto, HttpStatus.OK);
    }

    /**
     * Gets the {@link List list} of {@link BagDto bags}
     * 
     * @return the bag list
     */
    @GetMapping
    public ResponseEntity<List<BagDto>> list() {
        final List<BagDto> bags = list(API_RESOURCE_PATH, BagDto.class);
        return new ResponseEntity<>(bags, HttpStatus.OK);
    }

}

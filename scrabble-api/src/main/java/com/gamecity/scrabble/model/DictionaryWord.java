package com.gamecity.scrabble.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A dictionary word represents a word with its definition in the dictionary
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class DictionaryWord {

    private String word;
    private String definition;

}

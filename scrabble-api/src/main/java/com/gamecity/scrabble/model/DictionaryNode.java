package com.gamecity.scrabble.model;

import java.util.HashMap;

import com.gamecity.scrabble.entity.Language;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A dictionary node represents a letter node in a {@link Dictionary dictionary}
 */
@Data
@AllArgsConstructor
public class DictionaryNode {

    /*
     * children of the node
     */
    private HashMap<Character, DictionaryNode> children;

    /*
     * definition of the word if the node is the end of a word
     */
    private String definition;

    /*
     * whether the node is the end of a word
     */
    private boolean endOfWord;

    /**
     * Creates a new node with the given language properties
     * 
     * @param language language of the dictionary
     */
    public DictionaryNode(final Language language) {
        this.children = new HashMap<>(language.getAlphabetSize());
    }
}
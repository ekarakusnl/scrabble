package com.gamecity.scrabble.model;

import com.gamecity.scrabble.entity.Language;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A dictionary is the stack of words in a {@link Language language}
 * 
 * @author ekarakus
 */
@Data
@AllArgsConstructor
public class Dictionary {

    /*
     * language of the dictionary
     */
    private final Language language;

    /*
     * root node of the dictionary
     */
    private final DictionaryNode root;

    /**
     * Creates an empty dictionary by the given <code>language</code>
     * 
     * @param language language of the dictionary
     */
    public Dictionary(final Language language) {
        this.language = language;
        this.root = new DictionaryNode(language);
    }

    /**
     * Add word to the dictionary with the given word definition
     * 
     * @param word       the word
     * @param definition definition of the word
     */
    public void add(String word, String definition) {
        DictionaryNode current = root;

        // adds child nodes for each letter in the word
        for (char letter : word.toCharArray()) {
            current = current.getChildren().computeIfAbsent(letter, c -> new DictionaryNode(language));
        }

        current.setDefinition(definition);
        current.setEndOfWord(true);
    }

    /**
     * Search the given word in the dictionary
     * 
     * @param word the word to search
     * @return true if the word exists
     */
    public boolean search(String word) {
        DictionaryNode current = root;

        for (char letter : word.toCharArray()) {
            DictionaryNode node = current.getChildren().get(letter);
            if (node == null) {
                return false;
            }
            current = node;
        }

        return current.isEndOfWord();
    }

    /**
     * @param word the word to search
     * @return the word in the dictionary
     */
    public DictionaryWord get(String word) {
        DictionaryNode current = root;

        for (char letter : word.toCharArray()) {
            DictionaryNode node = current.getChildren().get(letter);
            if (node == null) {
                return null;
            }
            current = node;
        }

        // TODO add a test
        if (!current.isEndOfWord()) {
            return null;
        }

        return DictionaryWord.builder().word(word.toUpperCase()).definition(current.getDefinition()).build();
    }
}
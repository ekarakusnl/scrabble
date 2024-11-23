package com.gamecity.scrabble.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.model.Dictionary;
import com.gamecity.scrabble.model.DictionaryWord;
import com.gamecity.scrabble.service.DictionaryService;

import lombok.extern.slf4j.Slf4j;

@Service(value = "dictionaryService")
@Slf4j
class DictionaryServiceImpl implements DictionaryService {

    private final List<Dictionary> dictionaries;

    public DictionaryServiceImpl(@Value("${dictionary.path}") final String dictionaryPath) {
        this.dictionaries = new ArrayList<>(Language.values().length);
        initializeDictionaries(dictionaryPath);
    }

    private void initializeDictionaries(final String dictionaryPath) {
        Arrays.stream(Language.values()).forEach(language -> {
            final Dictionary dictionary = new Dictionary(language);
            Scanner scanner = null;
            try {
                final File file = new File(String.format(dictionaryPath, language));
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    final String content = scanner.nextLine();
                    if (content.toLowerCase().contains("\t")) {
                        final String selectedWord = StringUtils.substringBefore(content, "\t");
                        // TODO add a test
                        if (selectedWord.chars().allMatch(Character::isLetter)) {
                            final String definition = StringUtils.substringAfter(content, "\t");
                            dictionary.add(selectedWord.toUpperCase(), definition);
                        }
                    } else if (content.toLowerCase().contains(" ")) {
                        final String selectedWord = StringUtils.substringBefore(content, " ");
                        if (selectedWord.chars().allMatch(Character::isLetter)) {
                            final String definition = StringUtils.substringAfter(content, " ");
                            dictionary.add(selectedWord.toUpperCase(), definition);
                        }
                        // TODO add a test
                    } else if (content.chars().allMatch(Character::isLetter) && content.trim().length() > 1) {
                        dictionary.add(content.toUpperCase(), null);
                    }
                }
                dictionaries.add(dictionary);
            } catch (FileNotFoundException e) {
                log.error("An error occured while validation the word", e);
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        });
    }

    @Override
    public DictionaryWord get(String word, Language language) {
        final Dictionary dictionary = dictionaries.stream()
                .filter(d -> d.getLanguage().equals(language))
                .findFirst()
                .orElse(null);

        if (dictionary == null) {
            return null;
        }

        return dictionary.get(word.toUpperCase());
    }

    @Override
    public boolean find(String word, Language language) {
        final Dictionary dictionary = dictionaries.stream()
                .filter(d -> d.getLanguage().equals(language))
                .findFirst()
                .orElse(null);

        if (dictionary == null) {
            return false;
        }

        return dictionary.search(word.toUpperCase());
    }

}

package com.gamecity.scrabble.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.service.DictionaryService;
import com.google.common.io.Files;

import lombok.extern.slf4j.Slf4j;

@Service(value = "dictionaryService")
@Slf4j
class DictionaryServiceImpl implements DictionaryService {

    private static final Map<Language, List<String>> wordMap = new HashMap<>();

    @Value("${dictionary.path}")
    private String dictionaryPath;

    /**
     * Initializes and stores the dictionary words in a map for each language
     */
    @PostConstruct
    public void init() {
        Arrays.stream(Language.values()).forEach(this::loadWords);
    }

    private void loadWords(Language language) {
        try {
            final List<String> words =
                    Files.readLines(new File(String.format(dictionaryPath, language)), StandardCharsets.UTF_8);
            final List<String> lowercaseWords = words.stream().map(String::toLowerCase).collect(Collectors.toList());
            wordMap.put(language, lowercaseWords);
        } catch (FileNotFoundException e) {
            log.error("Dictionary path {} is not found for the language {}", String.format(dictionaryPath, language),
                    language, e.getMessage());
        } catch (IOException e) {
            log.error("An error occured while reading the dictionary for the language {}", language, e.getMessage());
        }
    }

    @Override
    public boolean hasWord(String word, Language language) {
        final List<String> words = wordMap.get(language);
        return words.contains(word.toLowerCase(new Locale(language.name())));
    }

}

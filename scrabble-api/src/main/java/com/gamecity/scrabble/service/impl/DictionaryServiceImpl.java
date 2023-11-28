package com.gamecity.scrabble.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.model.DictionaryWord;
import com.gamecity.scrabble.service.DictionaryService;

import lombok.extern.slf4j.Slf4j;

@Service(value = "dictionaryService")
@Slf4j
class DictionaryServiceImpl implements DictionaryService {

    private String dictionaryPath;

    public DictionaryServiceImpl(@Value("${dictionary.path}") final String dictionaryPath) {
        this.dictionaryPath = dictionaryPath;
    }

    @Override
    public DictionaryWord getWord(String word, Language language) {
        Scanner scanner = null;
        try {
            final File file = new File(String.format(dictionaryPath, language));
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                final String wordLine = scanner.nextLine();
                if (wordLine.toLowerCase().equals(word.toLowerCase())) {
                    return DictionaryWord.builder().word(wordLine.toUpperCase()).build();
                } else if (wordLine.toLowerCase().startsWith(word.toLowerCase() + "\t")) {
                    final String selectedWord = StringUtils.substringBefore(wordLine, "\t");
                    final String definition = StringUtils.substringAfter(wordLine, "\t");
                    return DictionaryWord.builder().word(selectedWord.toUpperCase()).definition(definition).build();
                } else if (wordLine.toLowerCase().startsWith(word.toLowerCase() + " ")) {
                    final String selectedWord = StringUtils.substringBefore(wordLine, " ");
                    final String definition = StringUtils.substringAfter(wordLine, " ");
                    return DictionaryWord.builder().word(selectedWord.toUpperCase()).definition(definition).build();
                }
            }
        } catch (FileNotFoundException e) {
            log.error("An error occured while validation the word", e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return null;
    }

}

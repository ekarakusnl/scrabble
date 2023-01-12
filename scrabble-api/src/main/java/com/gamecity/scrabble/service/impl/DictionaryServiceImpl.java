package com.gamecity.scrabble.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.service.DictionaryService;

import lombok.extern.slf4j.Slf4j;

@Service(value = "dictionaryService")
@Slf4j
class DictionaryServiceImpl implements DictionaryService {

    @Value("${dictionary.path}")
    private String dictionaryPath;

    @Override
    public boolean hasWord(String word, Language language) {
        try {
            final File file = new File(String.format(dictionaryPath, language));
            final Scanner scanner = new Scanner(file);
            boolean found = false;
            while (scanner.hasNextLine()) {
                final String dictionaryWord = scanner.nextLine();
                if (dictionaryWord.equalsIgnoreCase(word.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            scanner.close();
            return found;
        } catch (FileNotFoundException e) {
            log.error("An error occured while validation the word", e);
        }
        return false;
    }

}

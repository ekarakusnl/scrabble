package com.gamecity.scrabble.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.model.DictionaryWord;
import com.gamecity.scrabble.service.DictionaryService;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class TestDictionaryService extends AbstractServiceTest {

    @InjectMocks
    private DictionaryService dictionaryService = new DictionaryServiceImpl(
            "src/test/resources/dictionary/%s_dictionary.txt");

    @Test
    void test_get_word_with_definition_split_by_space() {
        final DictionaryWord dictionaryWord = dictionaryService.get("FARADAY", Language.fr);

        assertThat(dictionaryWord, notNullValue());
        assertThat(dictionaryWord.getWord(), equalTo("FARADAY"));
        assertThat(dictionaryWord.getDefinition(), equalTo("a unit used in chemistry"));
    }

    @Test
    void test_get_word_with_definition_split_by_special_char() {
        final DictionaryWord dictionaryWord = dictionaryService.get("WEAK", Language.fr);

        assertThat(dictionaryWord, notNullValue());
        assertThat(dictionaryWord.getWord(), equalTo("WEAK"));
        assertThat(dictionaryWord.getDefinition(), equalTo("feeble"));
    }

    @Test
    void test_get_word_without_definition() {
        final DictionaryWord dictionaryWord = dictionaryService.get("ROLE", Language.fr);

        assertThat(dictionaryWord, notNullValue());
        assertThat(dictionaryWord.getWord(), equalTo("ROLE"));
        assertThat(dictionaryWord.getDefinition(), nullValue());
    }

    @Test
    void test_get_invalid_word_split_by_space() {
        assertThat(dictionaryService.get("F'ARADAY", Language.fr), nullValue());
    }

    @Test
    void test_get_invalid_word_split_by_special_char() {
        assertThat(dictionaryService.get("W'EAK", Language.fr), nullValue());
    }

    @Test
    void test_get_invalid_word() {
        assertThat(dictionaryService.get("R'OLE", Language.fr), nullValue());
    }

    @Test
    void test_word_not_found() {
        assertThat(dictionaryService.get("CHEMISTRY", Language.fr), nullValue());
    }

    @Test
    void test_get_word_dictionary_not_found() {
        final DictionaryService dictionaryService = new DictionaryServiceImpl("test_dictionary.txt");

        assertThat(dictionaryService.get("ROLE", Language.fr), nullValue());
    }

    @Test
    void test_find_word_exist() {
        assertThat(dictionaryService.find("ROLE", Language.fr), equalTo(true));
    }

    @Test
    void test_find_word_does_not_exist() {
        assertThat(dictionaryService.find("ROL", Language.fr), equalTo(false));
    }

    @Test
    void test_find_word_dictionary_not_found() {
        final DictionaryService dictionaryService = new DictionaryServiceImpl("test_dictionary.txt");

        assertThat(dictionaryService.find("ROL", Language.fr), equalTo(false));
    }

}

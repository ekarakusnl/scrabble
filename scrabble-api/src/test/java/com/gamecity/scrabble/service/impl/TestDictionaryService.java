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
            "src/test/resources/dictionary/test_dictionary.txt");

    @Test
    void test_get_word_with_definition_split_by_space() {
        final DictionaryWord dictionaryWord = dictionaryService.getWord("FARADAY", Language.en);

        assertThat(dictionaryWord, notNullValue());
        assertThat(dictionaryWord.getWord(), equalTo("FARADAY"));
        assertThat(dictionaryWord.getDefinition(), equalTo("a unit used in chemistry"));
    }

    @Test
    void test_get_word_with_definition_split_by_special_char() {
        final DictionaryWord dictionaryWord = dictionaryService.getWord("WEAK", Language.en);

        assertThat(dictionaryWord, notNullValue());
        assertThat(dictionaryWord.getWord(), equalTo("WEAK"));
        assertThat(dictionaryWord.getDefinition(), equalTo("feeble"));
    }

    @Test
    void test_get_word_without_definition() {
        final DictionaryWord dictionaryWord = dictionaryService.getWord("ROLE", Language.en);

        assertThat(dictionaryWord, notNullValue());
        assertThat(dictionaryWord.getWord(), equalTo("ROLE"));
        assertThat(dictionaryWord.getDefinition(), nullValue());
    }

    @Test
    void test_word_not_found() {
        final DictionaryWord dictionaryWord = dictionaryService.getWord("CHEMISTRY", Language.en);

        assertThat(dictionaryWord, nullValue());
    }

    @Test
    void test_dictionary_not_found() {
        final DictionaryWord dictionaryWord = new DictionaryServiceImpl("test_dictionary.txt").getWord("ROLE",
                Language.en);

        assertThat(dictionaryWord, nullValue());
    }

}

package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.WordDao;
import com.gamecity.scrabble.entity.Word;

@Repository(value = "wordDao")
class WordDaoImpl extends AbstractDaoImpl<Word> implements WordDao {

    @Override
    public List<Word> getWords(Long gameId) {
        return listByNamedQuery(Constants.NamedQuery.getWords, Arrays.asList(Pair.of("gameId", gameId)));
    }

}

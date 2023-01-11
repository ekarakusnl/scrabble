package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.WordDao;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.service.WordService;

@Service(value = "wordService")
class WordServiceImpl extends AbstractServiceImpl<Word, WordDao> implements WordService {

    @Override
    public List<Word> getWords(Long gameId) {
        return baseDao.getWords(gameId);
    }

}

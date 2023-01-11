package com.gamecity.scrabble.resource.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.WordDto;
import com.gamecity.scrabble.resource.WordResource;
import com.gamecity.scrabble.service.WordService;

@Component(value = "wordResource")
class WordResourceImpl extends AbstractResourceImpl<Word, WordDto, WordService> implements WordResource {

    private WordService baseService;

    WordService getBaseService() {
        return baseService;
    }

    @Autowired
    void setBaseService(WordService baseService) {
        this.baseService = baseService;
    }

    @Override
    public Response list(Long gameId) {
        final List<Word> words = baseService.getWords(gameId);
        if (CollectionUtils.isEmpty(words)) {
            return Response.ok().build();
        }

        final List<WordDto> wordDtos = words.stream().map(Mapper::toDto).collect(Collectors.toList());
        return Response.ok(wordDtos).build();
    }

}

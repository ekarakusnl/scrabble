package com.gamecity.scrabble.resource.impl;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gamecity.scrabble.entity.Board;
import com.gamecity.scrabble.model.rest.BoardDto;
import com.gamecity.scrabble.resource.BoardResource;
import com.gamecity.scrabble.service.BoardService;

@Component(value = "boardResource")
class BoardResourceImpl extends AbstractResourceImpl<Board, BoardDto, BoardService> implements BoardResource {

    private BoardService baseService;

    BoardService getBaseService() {
        return baseService;
    }

    @Autowired
    void setBaseService(BoardService baseService) {
        this.baseService = baseService;
    }

    @Override
    public Response get(Long id) {
        return super.get(id);
    }

}

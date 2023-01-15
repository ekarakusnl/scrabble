package com.gamecity.scrabble.resource.impl;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.rest.VirtualBoardDto;
import com.gamecity.scrabble.resource.VirtualBoardResource;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.VirtualBoardService;

@Component(value = "virtualBoardResource")
class VirtualBoardResourceImpl implements VirtualBoardResource {

    private ActionService actionService;
    private VirtualBoardService virtualBoardService;

    @Autowired
    void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @Autowired
    void setVirtualBoardService(VirtualBoardService virtualBoardService) {
        this.virtualBoardService = virtualBoardService;
    }

    @Override
    public Response get(Long gameId, Integer actionCounter) {
        if (actionCounter < 1) {
            return Response.ok().build();
        }

        boolean hasNewAction = actionService.hasNewAction(gameId, actionCounter);
        if (!hasNewAction) {
            return Response.ok().build();
        }

        final VirtualBoard virtualBoard = virtualBoardService.getBoard(gameId, actionCounter);
        if (virtualBoard == null || CollectionUtils.isEmpty(virtualBoard.getCells())) {
            return Response.ok().build();
        }

        final VirtualBoardDto virtualBoardDto = Mapper.toDto(virtualBoard);
        return Response.ok(virtualBoardDto).build();
    }

}

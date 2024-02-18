package com.gamecity.scrabble.resource.impl;

import jakarta.ws.rs.core.Response;

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

    private VirtualBoardService virtualBoardService;
    private ActionService actionService;

    public VirtualBoardResourceImpl(final VirtualBoardService virtualBoardService, final ActionService actionService) {
        this.virtualBoardService = virtualBoardService;
        this.actionService = actionService;
    }

    @Override
    public Response get(Long gameId, Integer version) {
        if (version < 1) {
            return Response.ok().build();
        }

        boolean hasNewAction = actionService.hasNewAction(gameId, version);
        if (!hasNewAction) {
            return Response.ok().build();
        }

        final VirtualBoard virtualBoard = virtualBoardService.getBoard(gameId, version);
        if (virtualBoard == null || CollectionUtils.isEmpty(virtualBoard.getCells())) {
            return Response.ok().build();
        }

        final VirtualBoardDto virtualBoardDto = Mapper.toDto(virtualBoard);
        return Response.ok(virtualBoardDto).build();
    }

}

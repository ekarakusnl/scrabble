package com.gamecity.scrabble.service.impl;

import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.service.ContentService;
import com.gamecity.scrabble.service.VirtualBoardService;
import com.gamecity.scrabble.service.VirtualRackService;

@Service(value = "contentService")
class ContentServiceImpl implements ContentService {

    private VirtualBoardService virtualBoardService;
    private VirtualRackService virtualRackService;

    @Autowired
    void setVirtualBoardService(VirtualBoardService virtualBoardService) {
        this.virtualBoardService = virtualBoardService;
    }

    @Autowired
    void setVirtualRackService(VirtualRackService virtualRackService) {
        this.virtualRackService = virtualRackService;
    }

    @Override
    public void create(Game game) {
        // create the board
        virtualBoardService.createBoard(game.getId(), game.getBoardId());

        // create the racks of the players
        IntStream.range(1, game.getExpectedPlayerCount() + 1).forEach(playerNumber -> {
            virtualRackService.createRack(game.getId(), game.getLanguage(), playerNumber);
        });
    }

    @Override
    public void update(Game game, VirtualRack updatedRack, VirtualBoard updatedBoard, Integer playerNumber,
            Integer roundNumber) {
        virtualRackService.updateRack(game.getId(), playerNumber, roundNumber, updatedRack);
        virtualRackService.fillRack(game.getId(), game.getLanguage(), playerNumber, roundNumber + 1, updatedRack);
        virtualBoardService.updateBoard(game.getId(), updatedBoard);
    }

}

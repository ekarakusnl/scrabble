package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.service.UpdaterService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.VirtualBoardService;
import com.gamecity.scrabble.service.VirtualRackService;

@Service(value = "updaterService")
class UpdaterServiceImpl implements UpdaterService {

    private PlayerService playerService;
    private VirtualBoardService virtualBoardService;
    private VirtualRackService virtualRackService;
    private RedisRepository redisRepository;

    @Autowired
    void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Autowired
    void setVirtualBoardService(VirtualBoardService virtualBoardService) {
        this.virtualBoardService = virtualBoardService;
    }

    @Autowired
    void setVirtualRackService(VirtualRackService virtualRackService) {
        this.virtualRackService = virtualRackService;
    }

    @Autowired
    void setRedisRepository(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public void run(Action action, Game game) {
        if (ActionType.END == action.getType()) {
            redisRepository.publishAction(action.getGameId(), action);
            return;
        }

        final List<Player> players = updatePlayers(action);

        // the game is ready to start, prepare the board and the racks
        if (GameStatus.IN_PROGRESS == action.getStatus() && ActionType.START == action.getType()) {
            // create the board
            virtualBoardService.createBoard(game.getId(), game.getBoardId());

            // create the racks of the players
            players.stream().forEach(player -> {
                virtualRackService.createRack(game.getId(), game.getBagId(), player.getPlayerNumber());
            });
        }

        redisRepository.publishAction(action.getGameId(), action);
    }

    @Override
    public void run(Game game, VirtualRack updatedRack, VirtualBoard updatedBoard, Integer playerNumber,
            Integer roundNumber) {
        virtualRackService.updateRack(game.getId(), game.getBagId(), playerNumber, roundNumber, updatedRack);
        virtualRackService.refreshRack(game.getId(), game.getBagId(), playerNumber, roundNumber + 1, updatedRack);
        virtualBoardService.updateBoard(game.getId(), updatedBoard);
    }

    private List<Player> updatePlayers(Action action) {
        final List<Player> players = playerService.getPlayers(action.getGameId());
        redisRepository.updatePlayers(action.getGameId(), players);
        return players;
    }

}

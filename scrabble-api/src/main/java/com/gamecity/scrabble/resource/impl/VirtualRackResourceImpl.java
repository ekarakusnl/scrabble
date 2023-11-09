package com.gamecity.scrabble.resource.impl;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.model.rest.VirtualRackDto;
import com.gamecity.scrabble.model.rest.VirtualTileDto;
import com.gamecity.scrabble.resource.VirtualRackResource;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.VirtualRackService;

@Component(value = "virtualRackResource")
class VirtualRackResourceImpl implements VirtualRackResource {

    private VirtualRackService virtualRackService;
    private PlayerService playerService;
    private GameService gameService;

    @Autowired
    void setVirtualRackService(VirtualRackService virtualRackService) {
        this.virtualRackService = virtualRackService;
    }

    @Autowired
    void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Autowired
    void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Response get(Long gameId, Long userId, Integer roundNumber) {
        if (roundNumber < 1) {
            return Response.ok().build();
        }

        final Player player = playerService.getByUserId(gameId, userId);
        final VirtualRack virtualRack = virtualRackService.getRack(gameId, player.getPlayerNumber(), roundNumber);
        if (virtualRack == null || CollectionUtils.isEmpty(virtualRack.getTiles())) {
            return Response.ok().build();
        }

        final VirtualRackDto virtualRackDto = Mapper.toDto(virtualRack);
        return Response.ok(virtualRackDto).build();
    }

    @Override
    public Response exchangeTile(Long gameId, Long userId, Integer tileNumber) {

        final Game game = gameService.get(gameId);
        final Player player = playerService.getByUserId(gameId, userId);
        final VirtualTile virtualTile = virtualRackService.exchangeTile(gameId, game.getLanguage(),
                player.getPlayerNumber(), game.getRoundNumber(), tileNumber);

        final VirtualTileDto virtualTileDto = Mapper.toDto(virtualTile);
        return Response.ok(virtualTileDto).build();
    }

}

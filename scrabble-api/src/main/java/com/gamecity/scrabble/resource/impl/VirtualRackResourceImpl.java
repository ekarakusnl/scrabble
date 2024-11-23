package com.gamecity.scrabble.resource.impl;

import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.rest.VirtualRackDto;
import com.gamecity.scrabble.resource.VirtualRackResource;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.VirtualRackService;

@Component(value = "virtualRackResource")
class VirtualRackResourceImpl implements VirtualRackResource {

    private VirtualRackService virtualRackService;
    private PlayerService playerService;

    public VirtualRackResourceImpl(final VirtualRackService virtualRackService, final PlayerService playerService) {
        this.virtualRackService = virtualRackService;
        this.playerService = playerService;
    }

    @Override
    public Response get(Long gameId, Long userId, Integer roundNumber) {
        // TODO add a test
        if (roundNumber < 1) {
            return Response.ok().build();
        }

        final Player player = playerService.getByUserId(gameId, userId);
        final VirtualRack virtualRack = virtualRackService.getRack(gameId, player.getPlayerNumber(), roundNumber);
        // TODO add a test
        if (virtualRack == null || CollectionUtils.isEmpty(virtualRack.getTiles())) {
            return Response.ok().build();
        }

        final VirtualRackDto virtualRackDto = Mapper.toDto(virtualRack);
        return Response.ok(virtualRackDto).build();
    }

}

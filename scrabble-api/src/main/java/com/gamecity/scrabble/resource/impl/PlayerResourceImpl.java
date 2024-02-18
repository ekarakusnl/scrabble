package com.gamecity.scrabble.resource.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.PlayerDto;
import com.gamecity.scrabble.resource.PlayerResource;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.ActionService;

@Component(value = "playerResource")
class PlayerResourceImpl extends AbstractResourceImpl<Player, PlayerDto, PlayerService> implements PlayerResource {

    private ActionService actionService;

    public PlayerResourceImpl(final ActionService actionService) {
        this.actionService = actionService;
    }

    @Override
    public Response list(Long gameId, Integer version) {
        if (version < 1) {
            return Response.ok(Collections.emptyList()).build();
        }

        boolean hasNewAction = actionService.hasNewAction(gameId, version);
        if (!hasNewAction) {
            return Response.ok(Collections.emptyList()).build();
        }

        final List<Player> players = baseService.getPlayers(gameId);
        if (CollectionUtils.isEmpty(players)) {
            return Response.ok(Collections.emptyList()).build();
        }

        final List<PlayerDto> playerDtos = players.stream().map(Mapper::toDto).collect(Collectors.toList());
        return Response.ok(playerDtos).build();
    }

    @Override
    public Response get(Long gameId, Long userId) {
        final Player player = baseService.getByUserId(gameId, userId);
        return Response.ok(player).build();
    }

}

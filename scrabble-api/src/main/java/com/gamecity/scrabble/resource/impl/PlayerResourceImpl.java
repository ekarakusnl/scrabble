package com.gamecity.scrabble.resource.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.PlayerDto;
import com.gamecity.scrabble.resource.PlayerResource;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.ActionService;

@Component(value = "playerResource")
class PlayerResourceImpl extends AbstractResourceImpl<Player, PlayerDto, PlayerService> implements PlayerResource {

    private PlayerService baseService;
    private ActionService actionService;
    private RedisRepository redisRepository;

    PlayerService getBaseService() {
        return baseService;
    }

    @Autowired
    void setBaseService(PlayerService baseService) {
        this.baseService = baseService;
    }

    @Autowired
    void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @Autowired
    void setRedisRepository(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public Response list(Long gameId, Integer actionCounter) {
        if (actionCounter < 1) {
            return Response.ok().build();
        }

        boolean hasNewAction = actionService.hasNewAction(gameId, actionCounter);
        if (!hasNewAction) {
            return Response.ok().build();
        }

        final List<Player> players = redisRepository.getPlayers(gameId, actionCounter);
        if (CollectionUtils.isEmpty(players)) {
            return Response.ok().build();
        }

        final List<PlayerDto> playerDtos = players.stream().map(Mapper::toDto).collect(Collectors.toList());
        return Response.ok(playerDtos).build();
    }

}

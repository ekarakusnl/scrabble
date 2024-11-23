package com.gamecity.scrabble.resource.impl;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.VirtualRackDto;
import com.gamecity.scrabble.resource.GameResource;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.SchedulerService;

@Component(value = "gameResource")
class GameResourceImpl extends AbstractResourceImpl<Game, GameDto, GameService> implements GameResource {

    private ActionService actionService;
    private SchedulerService schedulerService;
    private RedisRepository redisRepository;

    public GameResourceImpl(final ActionService actionService, final SchedulerService schedulerService,
                            final RedisRepository redisRepository) {
        this.actionService = actionService;
        this.schedulerService = schedulerService;
        this.redisRepository = redisRepository;
    }

    @Override
    public Response create(GameDto gameDto) {
        final Game game = baseService.save(Mapper.toEntity(gameDto));

        publishLastAction(game);

        schedulerService.scheduleTerminateGameJob(game.getId(), game.getCreatedDate());

        final GameDto responseDto = Mapper.toDto(game);
        return Response.ok(responseDto).tag(createETag(responseDto)).build();
    }

    @Override
    public Response join(Long id, Long userId) {
        final Game game = baseService.join(id, userId);

        publishLastAction(game);

        if (GameStatus.READY_TO_START == game.getStatus()) {
            schedulerService.scheduleStartGameJob(id);
        }

        return Response.ok(Mapper.toDto(game)).build();
    }

    @Override
    public Response leave(Long id, Long userId) {
        final Game game = baseService.leave(id, userId);

        publishLastAction(game);

        return Response.ok(Mapper.toDto(game)).build();
    }

    @Override
    public Response play(Long id, Long userId, VirtualRackDto rackDto) {
        final VirtualRack virtualRack = Mapper.toEntity(rackDto);

        final ActionType actionType = baseService.determineActionType(virtualRack);
        final Game game = baseService.play(id, userId, virtualRack, actionType);

        publishLastAction(game);

        schedulerService.terminateSkipTurnJob(id, game.getVersion() - 1);
        baseService.scheduleNextRoundJobs(game);

        return Response.ok(Mapper.toDto(game)).build();
    }

    @Override
    public Response search(Long userId, Boolean includeUser) {
        final List<Game> games = baseService.search(userId, includeUser);
        return Response.ok(games.stream().map(Mapper::toDto).collect(Collectors.toList())).build();
    }

    private void publishLastAction(Game game) {
        final Action action = actionService.getAction(game.getId(), game.getVersion());
        redisRepository.publishAction(game.getId(), action);
    }

}

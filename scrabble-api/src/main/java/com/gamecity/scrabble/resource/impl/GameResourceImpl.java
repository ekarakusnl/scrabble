package com.gamecity.scrabble.resource.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.gamecity.scrabble.model.rest.VirtualTileDto;
import com.gamecity.scrabble.resource.GameResource;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.SchedulerService;

@Component(value = "gameResource")
class GameResourceImpl extends AbstractResourceImpl<Game, GameDto, GameService> implements GameResource {

    private GameService baseService;
    private ActionService actionService;
    private SchedulerService schedulerService;
    private RedisRepository redisRepository;

    GameService getBaseService() {
        return baseService;
    }

    @Autowired
    void setBaseService(GameService baseService) {
        this.baseService = baseService;
    }

    @Autowired
    void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @Autowired
    void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @Autowired
    void setRedisRepository(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public Response get(Long gameId) {
        return super.get(gameId);
    }

    @Override
    public Response create(GameDto gameDto) {
        final Game game = baseService.save(Mapper.toEntity(gameDto));

        publishLastAction(game);

        schedulerService.scheduleTerminateGameJob(game.getId());

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
        final VirtualRack rack = Mapper.toEntity(rackDto);

        final ActionType actionType =
                rackDto.getTiles().stream().noneMatch(VirtualTileDto::isSealed) ? ActionType.SKIP : ActionType.PLAY;
        final Game game = baseService.play(id, userId, rack, actionType);

        publishLastAction(game);

        // terminate the previous skipTurnJob
        schedulerService.terminateSkipTurnJob(id, game.getVersion() - 1);
        if (GameStatus.READY_TO_END == game.getStatus()) {
            // the last round has been played, schedule the end game job
            schedulerService.scheduleEndGameJob(id);
        } else {
            final boolean isMaximumSkipCountReached =
                    actionService.isMaximumSkipCountReached(id, game.getExpectedPlayerCount());
            if (isMaximumSkipCountReached) {
                // maximum skip count in a row has been reached, schedule the end game job
                schedulerService.scheduleEndGameJob(id);
            } else {
                // schedule the skip turn job for the next turn
                schedulerService.scheduleSkipTurnJob(game);
            }
        }

        return Response.ok(Mapper.toDto(game)).build();
    }

    @Override
    public Response list(Long userId) {
        if (userId == null) {
            return super.list();
        }

        final List<Game> games = baseService.listByUser(userId);
        return Response.ok(games.stream().map(Mapper::toDto).collect(Collectors.toList())).build();
    }

    private void publishLastAction(Game game) {
        final Action action = actionService.getAction(game.getId(), game.getVersion());
        redisRepository.publishAction(game.getId(), action);
    }

}

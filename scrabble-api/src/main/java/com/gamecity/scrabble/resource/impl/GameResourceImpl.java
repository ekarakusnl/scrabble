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
import com.gamecity.scrabble.resource.GameResource;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.SchedulerService;
import com.gamecity.scrabble.service.UpdaterService;

@Component(value = "gameResource")
class GameResourceImpl extends AbstractResourceImpl<Game, GameDto, GameService> implements GameResource {

    private GameService baseService;
    private ActionService actionService;
    private UpdaterService updaterService;
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
    void setUpdaterService(UpdaterService updaterService) {
        this.updaterService = updaterService;
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

        final ActionType actionType = ActionType.JOIN;
        updaterService.run(game, actionType);

        final Action action = actionService.add(game.getId(), game.getOwnerId(), game.getActionCounter(), null,
                game.getRoundNumber(), actionType, GameStatus.WAITING);
        redisRepository.publishAction(action.getGameId(), action);

        final GameDto responseDto = Mapper.toDto(game);
        return Response.ok(responseDto).tag(createETag(responseDto)).build();
    }

    @Override
    public Response join(Long id, Long userId) {
        final Game game = baseService.join(id, userId);

        final ActionType actionType = ActionType.JOIN;
        updaterService.run(game, actionType);

        final Action action = actionService.add(game.getId(), userId, game.getActionCounter(), null,
                game.getRoundNumber(), actionType, game.getStatus());
        redisRepository.publishAction(action.getGameId(), action);

        return Response.ok(Mapper.toDto(game)).build();
    }

    @Override
    public Response getAction(Long id, Integer counter) {
        if (counter < 1) {
            return Response.ok().build();
        }

        boolean hasNewAction = actionService.hasNewAction(id, counter);
        if (!hasNewAction) {
            return Response.ok().build();
        }

        final Action action = redisRepository.getAction(id, counter);
        if (action == null) {
            return Response.ok().build();
        }

        return Response.ok(Mapper.toDto(action)).build();
    }

    @Override
    public Response leave(Long id, Long userId) {
        final Game game = baseService.leave(id, userId);

        final ActionType actionType = ActionType.LEAVE;
        updaterService.run(game, actionType);

        final Action action = actionService.add(game.getId(), userId, game.getActionCounter(), null,
                game.getRoundNumber(), actionType, GameStatus.WAITING);
        redisRepository.publishAction(action.getGameId(), action);

        return Response.ok(Mapper.toDto(game)).build();
    }

    @Override
    public Response play(Long id, Long userId, VirtualRackDto rackDto) {
        final VirtualRack rack = Mapper.toEntity(rackDto);
        final Game game = baseService.play(id, userId, rack);

        final ActionType actionType = ActionType.PLAY;
        updaterService.run(game, actionType);

        final Action action = actionService.add(game.getId(), userId, game.getActionCounter(),
                game.getCurrentPlayerNumber(), game.getRoundNumber(), actionType, game.getStatus());
        redisRepository.publishAction(action.getGameId(), action);

        schedulerService.schedulePlayDuration(game.getId(), game.getCurrentPlayerNumber(), game.getDuration(),
                game.getActionCounter(), action.getCreatedDate());

        return Response.ok(Mapper.toDto(game)).build();
    }

    @Override
    public Response start(Long id) {
        final Game game = baseService.start(id);

        final ActionType actionType = ActionType.START;
        updaterService.run(game, actionType);

        final Action action = actionService.add(game.getId(), game.getOwnerId(), game.getActionCounter(),
                game.getCurrentPlayerNumber(), game.getRoundNumber(), actionType, GameStatus.IN_PROGRESS);
        redisRepository.publishAction(action.getGameId(), action);

        schedulerService.schedulePlayDuration(game.getId(), game.getCurrentPlayerNumber(), game.getDuration(),
                game.getActionCounter(), action.getCreatedDate());

        return Response.ok(Mapper.toDto(game)).build();
    }

    @Override
    public Response end(Long id) {
        final Game game = baseService.end(id);

        final Action action = actionService.add(game.getId(), game.getOwnerId(), game.getActionCounter(),
                game.getCurrentPlayerNumber(), game.getRoundNumber(), ActionType.END, GameStatus.ENDED);
        redisRepository.publishAction(action.getGameId(), action);

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

}

package com.gamecity.scrabble.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.SchedulerService;
import com.gamecity.scrabble.service.VirtualRackService;

/**
 * A Quartz {@link Job job} to skip the turn of a {@link Player player} at the end of the play
 * duration in a {@link Game game}
 * 
 * @author ekarakus
 */
@Component
public class SkipTurnJob implements Job {

    private ActionService actionService;
    private GameService gameService;
    private PlayerService playerService;
    private VirtualRackService virtualRackService;
    private SchedulerService schedulerService;
    private RedisRepository redisRepository;

    @Autowired
    void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @Autowired
    void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Autowired
    void setVirtualRackService(VirtualRackService virtualRackService) {
        this.virtualRackService = virtualRackService;
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
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        final Long gameId = dataMap.getLong("gameId");
        final Integer playerNumber = dataMap.getInt("playerNumber");
        final Integer version = dataMap.getInt("version");

        final Game game = gameService.get(gameId);

        if (game.getVersion() > version) {
            // player has already played, do not do anything
            return;
        }

        final Player player = playerService.getByPlayerNumber(gameId, playerNumber);
        final VirtualRack virtualRack = virtualRackService.getRack(gameId, playerNumber, game.getRoundNumber());

        final ActionType actionType = ActionType.TIMEOUT;
        final Game updatedGame = gameService.play(game.getId(), player.getUserId(), virtualRack, actionType);
        if (updatedGame == null) {
            return;
        }

        final Action action = actionService.getAction(gameId, updatedGame.getVersion());
        redisRepository.publishAction(action.getGameId(), action);

        if (GameStatus.READY_TO_END == updatedGame.getStatus()) {
            // the last round has been played, schedule the end game job
            schedulerService.scheduleEndGameJob(updatedGame.getId());
        } else {
            // schedule the skip turn job for the next turn
            schedulerService.scheduleSkipTurnJob(updatedGame);
        }
    }

}

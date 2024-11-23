package com.gamecity.scrabble.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.SchedulerService;

/**
 * A Quartz {@link Job job} to start a game when the status of a {@link Game game} is ready to start
 * 
 * @author ekarakus
 */
@Component
public class StartGameJob implements Job {

    private ActionService actionService;
    private GameService gameService;
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
        final Game game = gameService.start(gameId);

        final Action action = actionService.getAction(gameId, game.getVersion());
        redisRepository.publishAction(action.getGameId(), action);

        // terminate the scheduled game termination since the game is started
        schedulerService.terminateTerminateGameJob(game.getId());

        gameService.scheduleNextRoundJobs(game);
    }

}

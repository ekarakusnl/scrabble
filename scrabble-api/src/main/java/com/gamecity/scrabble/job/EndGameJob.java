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

/**
 * A Quartz {@link Job job} to end a game when the status of a {@link Game game} is ready to end
 * 
 * @author ekarakus
 */
@Component
public class EndGameJob implements Job {

    private ActionService actionService;
    private GameService gameService;
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
    void setRedisRepository(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        final Long gameId = dataMap.getLong("gameId");
        final Game game = gameService.end(gameId);

        final Action action = actionService.getAction(gameId, game.getVersion());
        redisRepository.publishAction(action.getGameId(), action);
    }

}

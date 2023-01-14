package com.gamecity.scrabble.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.resource.GameResource;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.VirtualRackService;

import lombok.extern.slf4j.Slf4j;

/**
 * A Quartz {@link Job job} that is triggered at the end of play duration of a {@link Player player} in a
 * {@link Game game}
 * 
 * @author ekarakus
 */
@Component
@Slf4j
public class PlayDurationJob implements Job {

    private GameResource gameResource;
    private GameService gameService;
    private PlayerService playerService;
    private VirtualRackService virtualRackService;

    @Autowired
    void setGameResource(GameResource gameResource) {
        this.gameResource = gameResource;
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

    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        final Long gameId = dataMap.getLong("gameId");
        final Integer playerNumber = dataMap.getInt("playerNumber");
        final Integer actionCounter = dataMap.getInt("actionCounter");

        final Game game = gameService.get(gameId);

        if (game.getActionCounter() > actionCounter) {
            // player has already played, do not do anything
            return;
        }

        final Player player = playerService.getByPlayerNumber(gameId, playerNumber);
        final VirtualRack virtualRack = virtualRackService.getRack(gameId, playerNumber, game.getRoundNumber());

        gameResource.play(gameId, player.getUserId(), Mapper.toDto(virtualRack));

        log.debug("Round has been skipped for player {} on game {}", player.getUserId(), gameId);
    }
}

package com.gamecity.scrabble.service;

import org.quartz.Job;
import org.quartz.Scheduler;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;

/**
 * Provides services for {@link Scheduler scheduler} {@link Job jobs}
 * 
 * @author ekarakus
 */
public interface SchedulerService {

    /**
     * Schedules a {@link Job job} to run at the end of the play duration of the {@link Player player} in a
     * {@link Game game} to skip the turn
     * 
     * @param game the game
     */
    void scheduleSkipTurnJob(Game game);

    /**
     * Terminates the scheduled {@link Job job}
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the action
     */
    void terminateSkipTurnJob(Long gameId, Integer version);

    /**
     * Schedules a {@link Job job} to start the {@link Game game} when the game status is ready to start
     * 
     * @param gameId <code>id</code> of the game
     */
    void scheduleStartGameJob(Long gameId);

    /**
     * Schedules a {@link Job job} to end the {@link Game game} when the game status is ready to end
     * 
     * @param gameId <code>id</code> of the game
     */
    void scheduleEndGameJob(Long gameId);

}

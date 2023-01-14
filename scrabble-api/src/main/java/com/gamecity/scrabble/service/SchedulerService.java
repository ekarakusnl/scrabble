package com.gamecity.scrabble.service;

import java.util.Date;

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
     * Schedules a {@link Job job} to run at the end of the play duration of a {@link Player player} in a
     * {@link Game game}
     * 
     * @param gameId        <code>id</code> of the game
     * @param playerNumber  <code>number</code> of the player
     * @param duration      <code>duration</code> of the play
     * @param actionCounter <code>counter</code> of the action
     * @param actionDate    <code>date</code> of the action
     */
    void schedulePlayDuration(Long gameId, Integer playerNumber, Integer duration, Integer actionCounter,
            Date actionDate);

}

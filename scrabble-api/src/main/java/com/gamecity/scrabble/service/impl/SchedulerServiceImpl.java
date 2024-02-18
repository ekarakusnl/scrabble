package com.gamecity.scrabble.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.job.EndGameJob;
import com.gamecity.scrabble.job.SkipTurnJob;
import com.gamecity.scrabble.job.StartGameJob;
import com.gamecity.scrabble.job.TerminateGameJob;
import com.gamecity.scrabble.service.SchedulerService;

import lombok.extern.slf4j.Slf4j;

@Service("schedulerService")
@Slf4j
class SchedulerServiceImpl implements SchedulerService {

    public static final String PARAM_GAME_ID = "gameId";
    public static final String PARAM_PLAYER_NUMBER = "playerNumber";
    public static final String PARAM_VERSION = "version";

    private static final String SKIP_TURN_JOB_IDENTITY = "skipTurnJob_%s_%s";
    private static final String SKIP_TURN_JOB_GROUP = "skipTurn";
    private static final String SKIP_TURN_TRIGGER_IDENTITY = "skipTurnTrigger_%s_%s";

    private static final String START_GAME_JOB_IDENTITY = "startGameJob_%s";
    private static final String START_GAME_JOB_GROUP = "startGame";
    private static final String START_GAME_TRIGGER_IDENTITY = "startGameTrigger_%s";

    private static final String END_GAME_JOB_IDENTITY = "endGameJob_%s";
    private static final String END_GAME_JOB_GROUP = "endGame";
    private static final String END_GAME_TRIGGER_IDENTITY = "endGameTrigger_%s";

    private static final String TERMINATE_GAME_JOB_IDENTITY = "terminateGameJob_%s";
    private static final String TERMINATE_GAME_JOB_GROUP = "terminateGame";
    private static final String TERMINATE_GAME_TRIGGER_IDENTITY = "terminateGameTrigger_%s";

    private SchedulerFactoryBean schedulerFactory;

    public SchedulerServiceImpl(final SchedulerFactoryBean schedulerFactory) {
        this.schedulerFactory = schedulerFactory;
    }

    @Override
    public void scheduleSkipTurnJob(Game game) {
        try {
            final JobDetail jobDetail = JobBuilder.newJob(SkipTurnJob.class)
                    .withIdentity(String.format(SKIP_TURN_JOB_IDENTITY, game.getId(), game.getVersion()),
                            SKIP_TURN_JOB_GROUP)
                    .usingJobData(PARAM_GAME_ID, game.getId())
                    .usingJobData(PARAM_PLAYER_NUMBER, game.getCurrentPlayerNumber())
                    .usingJobData(PARAM_VERSION, game.getVersion())
                    .build();

            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(game.getLastUpdatedDate().atZone(ZoneId.systemDefault()).toInstant()));
            calendar.add(Calendar.SECOND, game.getDuration());

            final SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(String.format(SKIP_TURN_TRIGGER_IDENTITY, game.getId(), game.getVersion()),
                            SKIP_TURN_JOB_GROUP)
                    .startAt(calendar.getTime())
                    .forJob(String.format(SKIP_TURN_JOB_IDENTITY, game.getId(), game.getVersion()), SKIP_TURN_JOB_GROUP)
                    .build();

            final Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();

            log.info("SkipTurnJob has been created on game {} for version {}", game.getId(), game.getVersion());
        } catch (SchedulerException e) {
            log.error("An error occured while scheduling the skip turn job", e);
        }
    }

    @Override
    public void terminateSkipTurnJob(Long gameId, Integer version) {
        try {
            final JobKey jobKey = new JobKey(String.format(SKIP_TURN_JOB_IDENTITY, gameId, version),
                    SKIP_TURN_JOB_GROUP);

            schedulerFactory.getScheduler().interrupt(jobKey);
            schedulerFactory.getScheduler().deleteJob(jobKey);

            log.info("SkipTurnJob has been terminated on game {} for version {}", gameId, version);
        } catch (SchedulerException e) {
            log.error("An error occured while terminating the skip turn job", e);
        }
    }

    @Override
    public void scheduleStartGameJob(Long gameId) {
        try {
            final JobDetail jobDetail = JobBuilder.newJob(StartGameJob.class)
                    .withIdentity(String.format(START_GAME_JOB_IDENTITY, gameId), START_GAME_JOB_GROUP)
                    .usingJobData(PARAM_GAME_ID, gameId)
                    .build();

            final SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(String.format(START_GAME_TRIGGER_IDENTITY, gameId), START_GAME_JOB_GROUP)
                    .startNow()
                    .forJob(String.format(START_GAME_JOB_IDENTITY, gameId), START_GAME_JOB_GROUP)
                    .build();

            final Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();

            log.info("StartGameJob has been created on game {}", gameId);
        } catch (SchedulerException e) {
            log.error("An error occured while scheduling the start game job", e);
        }
    }

    @Override
    public void scheduleEndGameJob(Long gameId) {
        try {
            final JobDetail jobDetail = JobBuilder.newJob(EndGameJob.class)
                    .withIdentity(String.format(END_GAME_JOB_IDENTITY, gameId), END_GAME_JOB_GROUP)
                    .usingJobData(PARAM_GAME_ID, gameId)
                    .build();

            final SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(String.format(END_GAME_TRIGGER_IDENTITY, gameId), END_GAME_JOB_GROUP)
                    .startNow()
                    .forJob(String.format(END_GAME_JOB_IDENTITY, gameId), END_GAME_JOB_GROUP)
                    .build();

            final Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();

            log.info("EndGameJob has been created on game {}", gameId);
        } catch (SchedulerException e) {
            log.error("An error occured while scheduling the end game job", e);
        }
    }

    @Override
    public void scheduleTerminateGameJob(Long gameId, LocalDateTime createdDate) {
        try {
            final JobDetail jobDetail = JobBuilder.newJob(TerminateGameJob.class)
                    .withIdentity(String.format(TERMINATE_GAME_JOB_IDENTITY, gameId), TERMINATE_GAME_JOB_GROUP)
                    .usingJobData(PARAM_GAME_ID, gameId)
                    .build();

            // terminate the game if it doesn't start in 10 minutes
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant()));
            calendar.add(Calendar.MINUTE, Constants.Game.TERMINATE_GAME_DURATION_MINUTES);

            final SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(String.format(TERMINATE_GAME_TRIGGER_IDENTITY, gameId), TERMINATE_GAME_JOB_GROUP)
                    .startAt(calendar.getTime())
                    .forJob(String.format(TERMINATE_GAME_JOB_IDENTITY, gameId), TERMINATE_GAME_JOB_GROUP)
                    .build();

            final Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();

            log.info("TerminateGameJob has been created on game {}", gameId);
        } catch (SchedulerException e) {
            log.error("An error occured while scheduling the terminate game job", e);
        }
    }

    @Override
    public void terminateTerminateGameJob(Long gameId) {
        try {
            final JobKey jobKey = new JobKey(String.format(TERMINATE_GAME_JOB_IDENTITY, gameId),
                    TERMINATE_GAME_JOB_GROUP);

            schedulerFactory.getScheduler().interrupt(jobKey);
            schedulerFactory.getScheduler().deleteJob(jobKey);

            log.info("TerminateGameJob has been terminated on game {}", gameId);
        } catch (SchedulerException e) {
            log.error("An error occured while terminating the terminate game job", e);
        }
    }

}

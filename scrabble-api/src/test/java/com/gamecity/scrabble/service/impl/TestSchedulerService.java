package com.gamecity.scrabble.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.service.SchedulerService;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

class TestSchedulerService extends AbstractServiceTest {

    @Mock
    private SchedulerFactoryBean schedulerFactory;

    @InjectMocks
    private SchedulerService schedulerService = new SchedulerServiceImpl(schedulerFactory);

    @Test
    void test_schedule_skip_turn_job() throws SchedulerException {
        final Game game = Game.builder()
                .id(DEFAULT_GAME_ID)
                .currentPlayerNumber(DEFAULT_PLAYER_NUMBER)
                .version(DEFAULT_VERSION)
                .duration(DEFAULT_DURATION)
                .lastUpdatedDate(LocalDateTime.now())
                .build();

        when(schedulerFactory.getScheduler()).thenReturn(mock(Scheduler.class));

        schedulerService.scheduleSkipTurnJob(game, game.getDuration());

        final ArgumentCaptor<JobDetail> jobDetail = ArgumentCaptor.forClass(JobDetail.class);
        final ArgumentCaptor<SimpleTrigger> trigger = ArgumentCaptor.forClass(SimpleTrigger.class);

        verify(schedulerFactory.getScheduler()).scheduleJob(jobDetail.capture(), trigger.capture());

        final JobDataMap dataMap = jobDetail.getValue().getJobDataMap();

        assertThat(dataMap.get(SchedulerServiceImpl.PARAM_GAME_ID), equalTo(game.getId()));
        assertThat(dataMap.get(SchedulerServiceImpl.PARAM_PLAYER_NUMBER), equalTo(game.getCurrentPlayerNumber()));
        assertThat(dataMap.get(SchedulerServiceImpl.PARAM_VERSION), equalTo(game.getVersion()));

        final Calendar startAt = Calendar.getInstance();
        startAt.setTime(Date.from(game.getLastUpdatedDate().atZone(ZoneId.systemDefault()).toInstant()));
        startAt.add(Calendar.SECOND, game.getDuration());

        assertThat(trigger.getValue().getStartTime(), equalTo(startAt.getTime()));

        verify(schedulerFactory.getScheduler(), times(1)).start();
    }

    @Test
    void test_schedule_terminate_skip_turn_job() throws SchedulerException {
        when(schedulerFactory.getScheduler()).thenReturn(mock(Scheduler.class));

        schedulerService.terminateSkipTurnJob(DEFAULT_GAME_ID, DEFAULT_VERSION);

        verify(schedulerFactory.getScheduler(), times(1)).interrupt(any(JobKey.class));
        verify(schedulerFactory.getScheduler(), times(1)).deleteJob(any(JobKey.class));
    }

    @Test
    void test_schedule_start_game_job() throws SchedulerException, InterruptedException {
        when(schedulerFactory.getScheduler()).thenReturn(mock(Scheduler.class));

        final Date dateBeforeExecution = new Date();

        Thread.sleep(10);

        schedulerService.scheduleStartGameJob(DEFAULT_GAME_ID);

        Thread.sleep(10);

        final Date dateAfterExecution = new Date();

        final ArgumentCaptor<JobDetail> jobDetail = ArgumentCaptor.forClass(JobDetail.class);
        final ArgumentCaptor<SimpleTrigger> trigger = ArgumentCaptor.forClass(SimpleTrigger.class);

        verify(schedulerFactory.getScheduler()).scheduleJob(jobDetail.capture(), trigger.capture());

        assertThat(jobDetail.getValue().getJobDataMap().get(SchedulerServiceImpl.PARAM_GAME_ID),
                equalTo(DEFAULT_GAME_ID));
        assertThat(trigger.getValue().getStartTime(), greaterThan(dateBeforeExecution));
        assertThat(trigger.getValue().getStartTime(), lessThan(dateAfterExecution));

        verify(schedulerFactory.getScheduler(), times(1)).start();
    }

    @Test
    void test_schedule_end_game_job() throws SchedulerException, InterruptedException {
        when(schedulerFactory.getScheduler()).thenReturn(mock(Scheduler.class));

        final Date dateBeforeExecution = new Date();

        Thread.sleep(10);

        schedulerService.scheduleEndGameJob(DEFAULT_GAME_ID);

        Thread.sleep(10);

        final Date dateAfterExecution = new Date();

        final ArgumentCaptor<JobDetail> jobDetail = ArgumentCaptor.forClass(JobDetail.class);
        final ArgumentCaptor<SimpleTrigger> trigger = ArgumentCaptor.forClass(SimpleTrigger.class);

        verify(schedulerFactory.getScheduler()).scheduleJob(jobDetail.capture(), trigger.capture());

        assertThat(jobDetail.getValue().getJobDataMap().get(SchedulerServiceImpl.PARAM_GAME_ID),
                equalTo(DEFAULT_GAME_ID));
        assertThat(trigger.getValue().getStartTime(), greaterThan(dateBeforeExecution));
        assertThat(trigger.getValue().getStartTime(), lessThan(dateAfterExecution));

        verify(schedulerFactory.getScheduler(), times(1)).start();
    }

    @Test
    void test_schedule_terminate_game_job() throws SchedulerException {
        final LocalDateTime createdDate = LocalDateTime.now();

        when(schedulerFactory.getScheduler()).thenReturn(mock(Scheduler.class));

        schedulerService.scheduleTerminateGameJob(DEFAULT_GAME_ID, createdDate);

        final ArgumentCaptor<JobDetail> jobDetail = ArgumentCaptor.forClass(JobDetail.class);
        final ArgumentCaptor<SimpleTrigger> trigger = ArgumentCaptor.forClass(SimpleTrigger.class);

        verify(schedulerFactory.getScheduler()).scheduleJob(jobDetail.capture(), trigger.capture());

        final JobDataMap dataMap = jobDetail.getValue().getJobDataMap();

        assertThat(dataMap.get(SchedulerServiceImpl.PARAM_GAME_ID), equalTo(DEFAULT_GAME_ID));

        final Calendar startAt = Calendar.getInstance();
        startAt.setTime(Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant()));
        startAt.add(Calendar.MINUTE, Constants.Game.TERMINATE_GAME_DURATION_MINUTES);

        assertThat(trigger.getValue().getStartTime(), equalTo(startAt.getTime()));

        verify(schedulerFactory.getScheduler(), times(1)).start();
    }

    @Test
    void test_schedule_terminate_terminate_game_job() throws SchedulerException {
        when(schedulerFactory.getScheduler()).thenReturn(mock(Scheduler.class));

        schedulerService.terminateTerminateGameJob(DEFAULT_GAME_ID);

        verify(schedulerFactory.getScheduler(), times(1)).interrupt(any(JobKey.class));
        verify(schedulerFactory.getScheduler(), times(1)).deleteJob(any(JobKey.class));
    }

}

package com.gamecity.scrabble.service.impl;

import java.util.Calendar;
import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.job.PlayDurationJob;
import com.gamecity.scrabble.service.SchedulerService;

import lombok.extern.slf4j.Slf4j;

@Service("schedulerService")
@Slf4j
class SchedulerServiceImpl implements SchedulerService {

    private SchedulerFactoryBean schedulerFactory;

    @Autowired
    void setSchedulerFactory(SchedulerFactoryBean schedulerFactory) {
        this.schedulerFactory = schedulerFactory;
    }

    @Override
    public void schedulePlayDuration(Long gameId, Integer playerNumber, Integer duration, Integer actionCounter,
            Date actionDate) {
        try {
            final JobDetail jobDetail = JobBuilder.newJob(PlayDurationJob.class)
                    .withIdentity("job" + actionCounter, "durationTrigger")
                    .usingJobData("gameId", gameId)
                    .usingJobData("playerNumber", playerNumber)
                    .usingJobData("actionCounter", actionCounter)
                    .build();

            final Calendar cal = Calendar.getInstance();
            cal.setTime(actionDate);
            cal.add(Calendar.MINUTE, duration);

            final SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity("trigger" + actionCounter, "durationTrigger")
                    .startAt(cal.getTime())
                    .forJob("job" + actionCounter, "durationTrigger")
                    .build();

            final Scheduler scheduler = schedulerFactory.getScheduler();

            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();

        } catch (SchedulerException e) {
            log.debug("An error occured while triggering the scheduler job", e);
        }
    }

}

package com.gamecity.scrabble.config;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.gamecity.scrabble.job.AutowiringSpringBeanJobFactory;

/**
 * Spring configuration of Quartz scheduler
 * 
 * @author ekarakus
 */
@Configuration
public class QuartzConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    JobFactory jobFactory() {
        final AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    SchedulerFactoryBean schedulerFactory() {
        final SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setAutoStartup(true);
        factory.setSchedulerName("Default Scheduler");
        factory.setOverwriteExistingJobs(true);
        factory.setJobFactory(jobFactory());
        return factory;
    }

}

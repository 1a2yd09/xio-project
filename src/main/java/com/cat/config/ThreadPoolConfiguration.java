package com.cat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author CAT
 */
@Configuration
@EnableScheduling
@Profile({"native", "test", "xio", "mail"})
public class ThreadPoolConfiguration implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(6);
        scheduler.setThreadNamePrefix("at-scheduled-");
        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);
    }

    @Bean("serviceTaskScheduler")
    public ThreadPoolTaskScheduler serviceTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(6);
        scheduler.setThreadNamePrefix("service-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean("serviceTaskExecutor")
    public ThreadPoolTaskExecutor serviceTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setKeepAliveSeconds(0);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("service-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("mailTaskExecutor")
    @Profile("mail")
    public ThreadPoolTaskExecutor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setKeepAliveSeconds(0);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("mail-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
}

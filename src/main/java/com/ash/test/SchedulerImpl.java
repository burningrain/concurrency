package com.ash.test;

import com.ash.test.api.EventProcessor;
import com.ash.test.api.Scheduler;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by burningrain on 07.10.2018.
 */
public class SchedulerImpl implements Scheduler {

    private EventProcessor eventProcessor;

    private ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("scheduler-thread");
        return thread;
    });

    public SchedulerImpl(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @Override
    public void start() {
        timer.scheduleAtFixedRate(() -> {
            try {
                eventProcessor.handleBacklogEvents();
            } catch (Exception e) {
                e.printStackTrace(); // логируем ошибку
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        timer.shutdown();
    }

}
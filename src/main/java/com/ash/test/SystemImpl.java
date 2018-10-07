package com.ash.test;

import com.ash.test.api.EventProcessor;
import com.ash.test.api.Scheduler;
import com.ash.test.api.SomeSystem;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * Created by burningrain on 07.10.2018.
 */
public class SystemImpl<T> implements SomeSystem<T> {

    private Backlog<T> backlog;
    private EventProcessor<T> eventProcessor;
    private Scheduler scheduler;

    public SystemImpl() {
        backlog = new Backlog<>();
        eventProcessor = new EventProcessorImpl<>(backlog);
        scheduler = new SchedulerImpl(eventProcessor);
    }

    public void start() {
        scheduler.start();
    }

    @Override
    public void stop() {
        scheduler.stop();
    }

    @Override
    public void addToBacklog(LocalDateTime time, Callable<T> task) {
        backlog.add(time, task);
    }

}
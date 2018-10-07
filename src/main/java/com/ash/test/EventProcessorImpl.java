package com.ash.test;

import com.ash.test.api.EventProcessor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by burningrain on 07.10.2018.
 */
public class EventProcessorImpl<V> implements EventProcessor<V> {

    private Backlog<V> backlog;

    public EventProcessorImpl(Backlog<V> backlog) {
        this.backlog = backlog;
    }

    @Override
    public void handleBacklogEvents() {
        Collection<Event<V>> events = backlog.takeSoon(LocalDateTime.now());
        for (Event<V> vEvent : events) {
            try {
                vEvent.getCallable().call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
package com.ash.test;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by burningrain on 07.10.2018.
 */
public class Backlog<V> {

    private int count = 0;
    private TreeSet<Event<V>> events = new TreeSet<>();

    public void add(LocalDateTime time, Callable<V> task) {
        events.add(new Event<>(count, time, task));
        if(count == Integer.MAX_VALUE) {
            count = 0;
        } else {
            count++;
        }
    }

    public Result<V> takeSoon(LocalDateTime currentTime) {
        if (events.isEmpty() || currentTime.isBefore(events.first().getTime())) {
            return new Result<V>(Collections.emptyList(), events.first());
        }

        ArrayList<Event<V>> result = new ArrayList<>();
        Iterator<Event<V>> iterator = events.iterator();
        Event<V> afterEvent = null;

        while (iterator.hasNext()) {
            Event event = iterator.next();
            if (currentTime.isBefore(event.getTime())) {
                afterEvent = event;
                break;
            }

            result.add(event);
            iterator.remove();
        }

        return new Result(result, afterEvent);
    }

    public static class Result<V> {

        private final Collection<Event<V>> beforeAndNowEvents;
        private final Event<V> afterEvent;

        public Result(Collection<Event<V>> beforeAndNowEvents, Event<V> afterEvent) {
            this.beforeAndNowEvents = beforeAndNowEvents;
            this.afterEvent = afterEvent;
        }

        public Collection<Event<V>> getBeforeAndNowEvents() {
            return beforeAndNowEvents;
        }

        public Event<V> getAfterEvent() {
            return afterEvent;
        }

    }



}
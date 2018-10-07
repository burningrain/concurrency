package com.ash.test;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by burningrain on 07.10.2018.
 */
public class Backlog<V> {

    private int count = 0;
    private LocalDateTime soonDate;
    private TreeSet<Event> events = new TreeSet<>();

    public synchronized void add(LocalDateTime time, Callable<V> task) {
        events.add(new Event<>(count, time, task));
        if(count == Integer.MAX_VALUE) {
            count = 0;
        } else {
            count++;
        }
        updateSoonDate();
    }

    public synchronized Collection<Event<V>> takeSoon(LocalDateTime currentTime) {
        if(soonDate == null || currentTime.isBefore(soonDate)) {
            return Collections.emptyList();
        }

        ArrayList<Event<V>> result = new ArrayList<>();

        Iterator<Event> iterator = events.iterator();
        while(iterator.hasNext()) {
            Event event = iterator.next();
            if(currentTime.isBefore(event.getTime())) {
                break;
            }

            result.add(event);
            iterator.remove();
        }

        updateSoonDate();
        return result;
    }

    private void updateSoonDate() {
        if(events.isEmpty()) return;

        Event first = events.first();
        soonDate = first.getTime();
    }

}
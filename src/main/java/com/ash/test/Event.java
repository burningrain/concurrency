package com.ash.test;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * Created by burningrain on 07.10.2018.
 */
public class Event<V> implements Comparable<Event<V>> {

    private final int number;
    private final LocalDateTime time;
    private final Callable<V> callable;

    public Event(int number, LocalDateTime time, Callable<V> callable) {
        if(number < 0) {
            throw new IllegalArgumentException("Номер задачи не должен быть отрицательным! " +
                    "[number=" + number + ", time=" + time + "]");
        }

        this.number = number;
        this.time = time;
        this.callable = callable;
    }

    public int getNumber() {
        return number;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Callable<V> getCallable() {
        return callable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event<?> event = (Event<?>) o;

        if (number != event.number) return false;
        return time.equals(event.time);

    }

    @Override
    public int compareTo(Event<V> event) {
        int timeComp = this.time.compareTo(event.getTime());
        if(timeComp == 0) {
            return this.number - event.getNumber();
        }
        return timeComp;
    }

}
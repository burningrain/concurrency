package com.ash.test;

import com.ash.test.api.EventProcessor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by burningrain on 07.10.2018.
 */
public class EventProcessorImpl<V> implements EventProcessor<V> {


    @Override
    public void handleBacklogEvents(Collection<Event<V>> events) {
        for (Event<V> vEvent : events) {
            Object result;
            try {
                result = vEvent.getCallable().call();
            } catch (Throwable t) {
                t.printStackTrace();
                result = "exception";
            }
            System.out.println("EXECUTE : " + result + " " + LocalDateTime.now());
        }
    }

}
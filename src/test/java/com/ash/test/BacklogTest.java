package com.ash.test;

import com.ash.test.mock.MockCallable;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by burningrain on 07.10.2018.
 */
public class BacklogTest {

    @Test
    public void testTakeSoonEventsInOrderOfTime() {
        Backlog<String> backlog = new Backlog();

        LocalDateTime now = LocalDateTime.now();
        backlog.add(now, new MockCallable("событие 1"));
        backlog.add(now.minus(1, ChronoUnit.MINUTES), new MockCallable("событие 2"));
        backlog.add(now.plus(1, ChronoUnit.MINUTES), new MockCallable("событие 3"));
        backlog.add(now.minus(10, ChronoUnit.MINUTES), new MockCallable("событие 4"));

        Assert.assertArrayEquals(new String[]{"событие 4", "событие 2", "событие 1"}, takeResultFromBacklog(backlog, now).toArray());
    }

    @Test
    public void testTakeSoonEventsInOrderOfArrivalWhenTimeEquals() throws Exception {
        Backlog<String> backlog = new Backlog();

        String[] events = new String[]{"событие 1", "событие 2", "событие 3", "событие 4"};

        LocalDateTime now = LocalDateTime.now();
        for (String event : events) {
            backlog.add(now, new MockCallable(event));
        }

        List<String> result = takeResultFromBacklog(backlog, now);

        Assert.assertArrayEquals(events, result.toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEventThrowExceptionWhenNumberLessZero() {
        new Event(-1, null, null);
    }

    private static List<String> takeResultFromBacklog(Backlog<String> backlog, LocalDateTime now) {
        return backlog.takeSoon(now).stream().map(event -> {
            try {
                return event.getCallable().call();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).collect(Collectors.<String>toList());
    }


}

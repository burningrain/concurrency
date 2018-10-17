package com.ash.test;

import com.ash.test.mock.MockCallable;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by burningrain on 07.10.2018.
 */
public class BacklogTest {

    @Test
    public void testTakeResultOnlyAfterEvent() {
        Backlog<String> backlog = new Backlog();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlusTenSec = now.plus(10, ChronoUnit.SECONDS);

        MockCallable mockCallable = new MockCallable("событие в будущем");
        backlog.add(nowPlusTenSec, mockCallable);
        Backlog.Result<String> result = backlog.takeSoon(now);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getBeforeAndNowEvents().isEmpty());

        Event<String> afterEvent = result.getAfterEvent();
        Assert.assertNotNull(afterEvent);
        Assert.assertEquals(nowPlusTenSec, afterEvent.getTime());
        Assert.assertEquals(mockCallable, afterEvent.getCallable());
    }

    @Test
    public void testTakeResultWithBeforeEventsAndAfterEvent() {
        Backlog<String> backlog = new Backlog();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime nowMinusTenSec = now.minus(10, ChronoUnit.SECONDS);
        int beforeEventsCount = 4;
        for(int i = 0; i < beforeEventsCount; i++) {
            backlog.add(nowMinusTenSec, new MockCallable("событие в прошлом или настоящем"));
        }

        LocalDateTime nowPlusTenSec = now.plus(10, ChronoUnit.SECONDS);
        MockCallable[] mockCallables = new MockCallable[]{new MockCallable("событие в будущем 1"), new MockCallable("событие в будущем 2")};
        for(int i = 0; i < 2; i++) {
            backlog.add(nowPlusTenSec, mockCallables[i]);
        }

        Backlog.Result<String> result = backlog.takeSoon(now);
        Assert.assertNotNull(result);

        Collection<Event<String>> beforeAndNowEvents = result.getBeforeAndNowEvents();
        Assert.assertEquals(beforeEventsCount, beforeAndNowEvents.size());

        Event<String> afterEvent = result.getAfterEvent();
        Assert.assertNotNull(afterEvent);
        Assert.assertEquals(nowPlusTenSec, afterEvent.getTime());
        Assert.assertEquals(mockCallables[0], afterEvent.getCallable());
    }

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
        return backlog.takeSoon(now).getBeforeAndNowEvents().stream().map(event -> {
            try {
                return event.getCallable().call();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).collect(Collectors.<String>toList());
    }


}

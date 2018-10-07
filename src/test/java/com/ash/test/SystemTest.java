package com.ash.test;

import com.ash.test.api.SomeSystem;
import com.ash.test.mock.MockResult;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by burningrain on 07.10.2018.
 */
public class SystemTest {

    private ExecutorService executorService = Executors.newFixedThreadPool(100, new ThreadFactory() {

        private AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("user-thread-" + count.getAndIncrement());
            return thread;
        }
    });

    @Test
    public void test() throws InterruptedException {
        SomeSystem<MockResult> system = new SystemImpl<>();
        system.start();

        final LinkedBlockingQueue<MockResult> results = new LinkedBlockingQueue<>();
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    LocalDateTime sendTime = LocalDateTime.now();
                    int diff = ThreadLocalRandom.current().nextInt(-10, 10 + 1);
                    LocalDateTime eventTime = sendTime.plus(diff, ChronoUnit.SECONDS);

                    String name = Thread.currentThread().getName();
                    system.addToBacklog(eventTime, () -> {
                        MockResult mockResult = new MockResult(sendTime, diff, eventTime, LocalDateTime.now(), name);
                        results.add(mockResult);
                        return mockResult;
                    });

                    Thread.sleep(50);
                }

                return null;
            });
        }
        Thread.sleep(10_000);

        executorService.shutdownNow();
        while (!executorService.isTerminated());
        system.stop();

        Map<LocalDateTime, List<MockResult>> schedulerBags = results.stream()
                .collect(Collectors.groupingBy(
                        (MockResult mockResult) ->  mockResult.getHandleTime(),
                        LinkedHashMap::new,
                        Collectors.mapping(mockResult -> mockResult, Collectors.toList())))
                ;

        schedulerBags.values().forEach(mockResults -> {
            mockResults.sort((o1, o2) -> getTime(o1).compareTo(getTime(o2)));
        });
        List<MockResult> afterSorting = schedulerBags.values().stream().flatMap(mockResults -> mockResults.stream())
                .collect(Collectors.toList());

        Assert.assertArrayEquals(results.toArray(), afterSorting.toArray());
    }

    private static LocalDateTime getTime(MockResult mockResult) {
        return mockResult.getEventTime();
    }

}

package com.ash.test;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by burningrain on 15.10.2018.
 */
public class SystemTest {

    @Test
    public void testSystemExecuteEventsInRightOrder() throws InterruptedException {
        ArrayList<String> result = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        System.out.println("NOW: " + now);

        SystemImpl<String> system = new SystemImpl<>();

        System.out.println("NEW TASK:" + " 10 " + now.plus(10, ChronoUnit.SECONDS));
        system.addEventAndHandleBacklog(now.plus(10, ChronoUnit.SECONDS), () -> {
            result.add("+10");
            return "+10";
        });

        System.out.println("NEW TASK:" + " 20 " + now.plus(20, ChronoUnit.SECONDS));
        system.addEventAndHandleBacklog(now.plus(20, ChronoUnit.SECONDS), () -> {
            Thread.sleep(10_000);
            result.add("+20");
            return "+20";
        });

        System.out.println("NEW TASK:" + " 30 " + now.plus(30, ChronoUnit.SECONDS));
        system.addEventAndHandleBacklog(now.plus(30, ChronoUnit.SECONDS), () -> {
            result.add("+30");
            return "+30";
        });

        System.out.println("NEW TASK:" + " 2_1 " + now.plus(2, ChronoUnit.SECONDS));
        system.addEventAndHandleBacklog(now.plus(2, ChronoUnit.SECONDS), () -> {
            result.add("+2_1");
            return "+2_1";
        });

        System.out.println("NEW TASK:" + " 2_2 " + now.plus(2, ChronoUnit.SECONDS));
        system.addEventAndHandleBacklog(now.plus(2, ChronoUnit.SECONDS), () -> {
            result.add("+2_2");
            throw new IllegalArgumentException("+2_2" + Thread.currentThread().getName());
        });

        System.out.println("NEW TASK:" + " 2_3 " + now.plus(2, ChronoUnit.SECONDS));
        system.addEventAndHandleBacklog(now.plus(2, ChronoUnit.SECONDS), () -> {
            result.add("+2_3");
            throw new IllegalArgumentException("+2_3" + Thread.currentThread().getName());
        });

        System.out.println("NEW TASK:" + " now " + now);
        system.addEventAndHandleBacklog(now, () -> {
            result.add("now");
            return "now";
        });

        System.out.println("NEW TASK:" + " -3_1 " + now.minus(3, ChronoUnit.SECONDS));
        system.addEventAndHandleBacklog(now.minus(3, ChronoUnit.SECONDS), () -> {
            result.add("-3_1");
            return "-3_1";
        });

        System.out.println("NEW TASK:" + " -3_2 " + now.minus(3, ChronoUnit.SECONDS));
        system.addEventAndHandleBacklog(now.minus(3, ChronoUnit.SECONDS), () -> {
            result.add("-3_2");
            return "-3_2";
        });

        System.out.println("NEW TASK:" + " -3_3 " + now.minus(3, ChronoUnit.SECONDS));
        system.addEventAndHandleBacklog(now.minus(3, ChronoUnit.SECONDS), () -> {
            result.add("-3_3");
            return "-3_3";
        });

        Thread.sleep(35_000);
        system.stop();

        System.out.println(result);
        Assert.assertArrayEquals(new String[]{
                        "now",
                        "-3_1",
                        "-3_2",
                        "-3_3",
                        "+2_1",
                        "+2_2",
                        "+2_3",
                        "+10",
                        "+20",
                        "+30",
                },
                result.toArray());
    }

    @Test
    public void testSystemExecuteEventsInRightOrderConcurrency() throws InterruptedException {
        final ArrayList<String> result = new ArrayList<>();

        SystemImpl<String> system = new SystemImpl<>();
        LocalDateTime now = LocalDateTime.now();
        System.out.println("NOW: " + now);
        final int count = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(count, new ThreadFactory() {

            private AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("user-thread-" + counter.getAndIncrement());
                return thread;
            }
        });
        int amount = 5 * count;
        for (int i = 0; i < amount; i += 5) {
            int counter = i;
            executorService.submit(() -> {
                for (int j = 0; j < 5; j++) {
                    int diffTime = counter + j;
                    System.out.println("NEW TASK:" + " " + diffTime + " " + now.plus(diffTime, ChronoUnit.SECONDS));
                    system.addEventAndHandleBacklog(now.plus(diffTime, ChronoUnit.SECONDS), () -> {
                        String s = ((diffTime < 10) ? "0" : "") + diffTime;
                        result.add(s);
                        return s;
                    });
                }
            });
        }
        Thread.sleep(30_000);
        executorService.shutdownNow();
        while (!executorService.isTerminated());
        system.stop();

        Assert.assertEquals(amount, result.size());

        ArrayList<String> sortedResult = new ArrayList<>();
        sortedResult.addAll(result);
        Collections.sort(sortedResult);

        Assert.assertArrayEquals(result.toArray(), sortedResult.toArray());
    }


}
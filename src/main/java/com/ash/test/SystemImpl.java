package com.ash.test;

import com.ash.test.api.EventProcessor;
import com.ash.test.api.SomeSystem;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by burningrain on 07.10.2018.
 */
public class SystemImpl<T> implements SomeSystem<T> {

    private EventProcessor<T> eventProcessor = new EventProcessorImpl<T>();
    private ExecutorService singleThreadExecutor;
    private ScheduledThreadPoolExecutor singleThreadScheduledExecutor;

    // потокобезопасно, т.к. работа всегда в 1 потоке
    private Backlog<T> backlog = new Backlog<>();
    private ScheduledFuture<?> scheduledFuture;
    private Event<T> soonAfterEvent;

    public SystemImpl() {
        singleThreadExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("event-handler-thread");
            return thread;
        });
        singleThreadScheduledExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("scheduler-event-thread");
            return thread;
        });
        singleThreadScheduledExecutor.setRemoveOnCancelPolicy(true);
    }

    public void stop() {
        singleThreadExecutor.shutdownNow();
        singleThreadScheduledExecutor.shutdownNow();
        while(!(singleThreadExecutor.isTerminated() && singleThreadScheduledExecutor.isTerminated()));
    }

    @Override
    public void addEventAndHandleBacklog(final LocalDateTime time, final Callable<T> task) {
        singleThreadExecutor.submit((VoidCallable) () -> {
            backlog.add(time, task);
            handleBacklog();
        });
    }

    private void handleBacklog() {
        LocalDateTime now = LocalDateTime.now();
        Backlog.Result<T> backlogResult = backlog.takeSoon(now);
        Event<T> afterEvent = backlogResult.getAfterEvent();

        if (afterEvent != null && afterEvent != soonAfterEvent) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }

            soonAfterEvent = afterEvent;
            scheduledFuture = singleThreadScheduledExecutor.schedule(
                    () -> {
                        singleThreadExecutor.submit((VoidCallable) () -> {
                            handleBacklog();
                        });
                    },
                    getDiffInMillis(afterEvent.getTime(), now),
                    TimeUnit.MILLISECONDS
            );
        }

        // обработка после планировки, вдруг долго будет обрабатываться
        Collection<Event<T>> beforeAndNowEvents = backlogResult.getBeforeAndNowEvents();
        if (!beforeAndNowEvents.isEmpty()) {
            eventProcessor.handleBacklogEvents(beforeAndNowEvents);
        }
    }

    private static long getDiffInMillis(LocalDateTime time1, LocalDateTime time2) {
        long milli1 = time1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long milli2 = time2.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return milli1 - milli2;
    }

}
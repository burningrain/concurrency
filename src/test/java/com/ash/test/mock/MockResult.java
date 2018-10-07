package com.ash.test.mock;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by burningrain on 07.10.2018.
 */
public class MockResult {

    private String uuid = UUID.randomUUID().toString();
    private final LocalDateTime sendTime;
    private final int diff; // in sec
    private final LocalDateTime eventTime;
    private final LocalDateTime handleTime;
    private final String threadName;

    public MockResult(LocalDateTime sendTime, int diff, LocalDateTime eventTime, LocalDateTime handleTime, String threadName) {
        this.sendTime = sendTime;
        this.diff = diff;
        this.eventTime = eventTime;
        this.handleTime = handleTime;
        this.threadName = threadName;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public int getDiff() {
        return diff;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public String getThreadName() {
        return threadName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockResult that = (MockResult) o;

        return uuid.equals(that.uuid);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("sendTime: ").append(sendTime)
                .append(" diff(sec): ").append(diff)
                .append(" eventTime: ").append(eventTime)
                .append(" handleTime: ").append(LocalDateTime.now())
                .append(" thread: ").append(threadName);
        return builder.toString();
    }

}

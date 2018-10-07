package com.ash.test.api;

import com.ash.test.Event;

/**
 * Created by burningrain on 07.10.2018.
 */
public interface EventProcessor<V> {

    void handleBacklogEvents();

}
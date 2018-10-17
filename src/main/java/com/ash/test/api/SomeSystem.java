package com.ash.test.api;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * Created by burningrain on 07.10.2018.
 */
public interface SomeSystem<T> {

    void addEventAndHandleBacklog(LocalDateTime time, Callable<T> task);

}
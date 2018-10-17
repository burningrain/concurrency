package com.ash.test;

import java.util.concurrent.Callable;

/**
 * Created by burningrain on 15.10.2018.
 */
public interface VoidCallable extends Callable<Void> {

    @Override
    default Void call() {
        try {
            exec();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    void exec() throws Exception;

}
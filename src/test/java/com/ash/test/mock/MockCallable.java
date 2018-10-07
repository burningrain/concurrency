package com.ash.test.mock;

import java.util.concurrent.Callable;

/**
 * Created by burningrain on 07.10.2018.
 */
public class MockCallable implements Callable<String> {

    private final String description;

    public MockCallable(String description) {
        this.description = description;
    }

    @Override
    public String call() throws Exception {
        return description;
    }
}

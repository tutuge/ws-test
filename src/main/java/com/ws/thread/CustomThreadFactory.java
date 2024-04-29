package com.ws.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class CustomThreadFactory implements ThreadFactory {
    private String threadNamePrefix;

    public CustomThreadFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName(threadNamePrefix + "-" + thread.getId());
        return thread;
    }
}

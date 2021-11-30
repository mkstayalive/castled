package io.castled.core;

import io.castled.functionalinterfaces.Action;
import io.castled.utils.ThreadUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IncessantRunner {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private volatile boolean shutdown = false;

    public IncessantRunner(Action action, long sleepMs) {
        executorService.execute(() -> {
            while (!shutdown) {
                action.execute();
                ThreadUtils.interruptIgnoredSleep(sleepMs);
            }
        });
    }

    public void shutdown(long awaitMs) {
        this.shutdown = true;
        executorService.shutdown();
        try {
            executorService.awaitTermination(awaitMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

}

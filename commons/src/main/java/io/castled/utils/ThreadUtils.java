package io.castled.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadUtils {

    public static void interruptIgnoredSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //no-op
        }

    }

    public static void terminateGracefully(ExecutorService executorService, long waitTimeoutSecs) {
        try {
            executorService.shutdown();
            executorService.awaitTermination(waitTimeoutSecs, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            //no-op
        }

    }
}

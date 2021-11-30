package io.castled.core;

import io.castled.exceptions.CastledRuntimeException;
import io.castled.utils.ThreadUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
public class CastledBlockingQueue<T> {

    private final Consumer<T> consumer;
    private final ExecutorService executorService;
    private final BlockingQueue<T> blockingQueue;
    private volatile boolean shutDown = false;

    private boolean exitOnError = false;
    private volatile Exception failureException;

    public CastledBlockingQueue(Consumer<T> childConsumer, int parallelism, int maxCapacity, boolean exitOnError) {
        this.consumer = decorateConsumer(childConsumer);
        this.executorService = Executors.newFixedThreadPool(parallelism);
        this.blockingQueue = new ArrayBlockingQueue<T>(maxCapacity);
        this.exitOnError = exitOnError;
        for (int i = 0; i < parallelism; i++) {
            executorService.execute(() -> {
                while (!shutDown) {
                    try {
                        T payload = blockingQueue.poll(1, TimeUnit.SECONDS);
                        if (payload != null) {
                            this.consumer.accept(payload);
                        }
                    } catch (Exception e) {
                        log.error("Blocking queue Consumer failed", e);
                        if (exitOnError) {
                            this.failureException = e;
                            break;
                        }
                    }
                }

            });

        }
    }

    public Consumer<T> decorateConsumer(Consumer<T> consumer) {
        return consumer;
    }

    public void writePayload(T payload, int timeout, TimeUnit timeUnit) throws TimeoutException {

        if (failureException != null) {
            shutdownNow();
            throw new CastledRuntimeException(failureException);
        }

        try {
            boolean success = blockingQueue.offer(payload, timeout, timeUnit);
            if (!success) {
                throw new TimeoutException();
            }
        } catch (InterruptedException e) {
            log.error("Blocking queue write payload interrupted", e);
            throw new CastledRuntimeException(e);
        }
    }

    public void flush(long timeOutMs) throws TimeoutException {

        while (!blockingQueue.isEmpty()) {
            if (exitOnError && failureException != null) {
                this.shutdownNow();
                throw new CastledRuntimeException(failureException);
            }
            ThreadUtils.interruptIgnoredSleep(500);
        }
        shutDown = true;
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        if (exitOnError && failureException != null) {
            this.shutdownNow();
            throw new CastledRuntimeException(failureException);
        }
    }

    private void shutdownNow() {
        this.shutDown = true;
        this.executorService.shutdownNow();
    }
}

package io.castled.kafka.consumer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InterruptException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConsumerUtils {

    public static void runKafkaConsumer(int consumerCount, String name, BaseKafkaConsumer baseKafkaConsumer) {
        ExecutorService executorService = Executors.newFixedThreadPool(consumerCount,
                new ThreadFactoryBuilder().setNameFormat(name + "-consumer-%d").build());
        for (int i = 0; i < consumerCount; i++) {
            executorService.submit(() -> {
                try {
                    baseKafkaConsumer.run();
                } catch (InterruptException e) {
                    log.warn("Consumer {} interrupted", name);

                } catch (Exception e) {
                    log.error("Consumer {} failed to run", name, e);
                }
            });
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (Exception e) {
                executorService.shutdownNow();
            }
        }));


    }
}

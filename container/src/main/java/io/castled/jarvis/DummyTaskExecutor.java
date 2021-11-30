package io.castled.jarvis;

import io.castled.jarvis.taskmanager.TaskExecutor;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.utils.ThreadUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DummyTaskExecutor implements TaskExecutor {
    @Override
    public String executeTask(Task task) {
        try {
            for (int i = 0; i < 60; i++) {
                System.out.println("printing "+i);
                ThreadUtils.interruptIgnoredSleep(1000);
            }
        } catch (Throwable e) {
            log.error("Dummy Executor failed", e);
        }
        return null;
    }
}

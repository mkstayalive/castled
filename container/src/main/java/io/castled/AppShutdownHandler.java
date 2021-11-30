package io.castled;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.jarvis.taskmanager.JarvisTasksClient;
import lombok.Getter;

@Singleton
public class AppShutdownHandler {

    private final JarvisTasksClient jarvisTasksClient;

    @Getter
    private volatile boolean shutdownTriggered;

    @Inject
    private AppShutdownHandler(JarvisTasksClient jarvisTasksClient) {
        this.jarvisTasksClient = jarvisTasksClient;
    }

    public void handleShutdown() {
        shutdownTriggered = true;
        this.jarvisTasksClient.close();
    }

}

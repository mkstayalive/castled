package io.castled.jarvis.taskmanager.models;

import com.google.common.collect.Lists;

import java.util.List;

public enum TaskStatus {
    QUEUED,
    PICKED,
    FAILED_TEMPORARILY,
    PROCESSED,
    DEFERRED,
    FAILED;

    public static List<TaskStatus> terminalStates() {
        return Lists.newArrayList(TaskStatus.PROCESSED, TaskStatus.FAILED);
    }

    public static List<TaskStatus> inProcessStates() {
        return Lists.newArrayList(QUEUED, PICKED, FAILED_TEMPORARILY);
    }

    public static List<TaskStatus> queuedStates() {
        return Lists.newArrayList(QUEUED, FAILED_TEMPORARILY);
    }
}

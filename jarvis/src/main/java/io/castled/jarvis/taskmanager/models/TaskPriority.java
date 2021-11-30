package io.castled.jarvis.taskmanager.models;

import lombok.Getter;

public enum TaskPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    PARAMOUNT(4);

    @Getter
    private final int rank;

    TaskPriority(int rank) {
        this.rank = rank;
    }

    public static TaskPriority getPriority(int rank) {
        for (TaskPriority taskPriority : TaskPriority.values()) {
            if (taskPriority.getRank() == rank) {
                return taskPriority;
            }
        }
        return null;
    }
}

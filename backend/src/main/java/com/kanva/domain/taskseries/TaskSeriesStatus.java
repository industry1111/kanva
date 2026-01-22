package com.kanva.domain.taskseries;

public enum TaskSeriesStatus {
    ACTIVE("활성"),
    STOPPED("중단");

    private final String description;

    TaskSeriesStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

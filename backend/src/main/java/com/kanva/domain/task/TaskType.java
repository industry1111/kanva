package com.kanva.domain.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskType {
    WORK("업무"),
    SCHEDULE("일정");

    private final String description;
}

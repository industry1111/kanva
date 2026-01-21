package com.kanva.domain.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskStatus {
    PENDING("대기"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료");

    private final String description;
}

package com.kanva.domain.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskCategory {
    WORK("할 일"),       // 업무
    SCHEDULE("일정"),   // 일정
    EXERCISE("운동"),   // 운동
    OTHER("기타");// 기타

    private final string description;
}

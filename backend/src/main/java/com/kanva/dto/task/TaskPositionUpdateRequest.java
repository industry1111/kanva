package com.kanva.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TaskPositionUpdateRequest {

    @NotNull(message = "Task ID 목록은 필수입니다")
    private List<Long> taskIds;

    @Builder
    public TaskPositionUpdateRequest(List<Long> taskIds) {
        this.taskIds = taskIds;
    }
}

package com.kanva.dto.task;

import com.kanva.domain.task.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TaskStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다")
    private TaskStatus status;

    @Builder
    public TaskStatusUpdateRequest(TaskStatus status) {
        this.status = status;
    }
}

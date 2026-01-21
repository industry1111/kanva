package com.kanva.dto.task;

import com.kanva.domain.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TaskRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다")
    private String title;

    private String description;

    private LocalDate dueDate;

    private TaskStatus status;

    private Integer position;

    @Builder
    public TaskRequest(String title, String description, LocalDate dueDate, TaskStatus status, Integer position) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.position = position;
    }
}

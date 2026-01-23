package com.kanva.dto.taskseries;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TaskSeriesRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    private String description;

    private LocalDate startDate;

    @NotNull(message = "종료일(dueDate)은 필수입니다")
    private LocalDate endDate;

    private Boolean stopOnComplete;
}

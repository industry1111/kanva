package com.kanva.dto.taskseries;

import com.kanva.domain.taskseries.TaskSeries;
import com.kanva.domain.taskseries.TaskSeriesStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TaskSeriesResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private TaskSeriesStatus status;
    private LocalDate stopDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskSeriesResponse from(TaskSeries series) {
        return TaskSeriesResponse.builder()
                .id(series.getId())
                .title(series.getTitle())
                .description(series.getDescription())
                .startDate(series.getStartDate())
                .endDate(series.getEndDate())
                .status(series.getStatus())
                .stopDate(series.getStopDate())
                .createdAt(series.getCreatedAt())
                .updatedAt(series.getUpdatedAt())
                .build();
    }
}

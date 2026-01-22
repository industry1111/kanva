package com.kanva.dto.taskseries;

import com.kanva.domain.taskseries.CompletionPolicy;
import com.kanva.domain.taskseries.TaskSeries;
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
    private CompletionPolicy completionPolicy;
    private boolean stopOnComplete;
    private LocalDate stopDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskSeriesResponse from(TaskSeries series) {
        return TaskSeriesResponse.builder()
                .id(series.getId())
                .title(series.getTitle())
                .description(series.getDescription())
                .startDate(series.getStartDate())
                .endDate(series.getEndDate())
                .completionPolicy(series.getCompletionPolicy())
                .stopOnComplete(series.getCompletionPolicy() == CompletionPolicy.COMPLETE_STOPS_SERIES)
                .stopDate(series.getStopDate())
                .active(series.isActive())
                .createdAt(series.getCreatedAt())
                .updatedAt(series.getUpdatedAt())
                .build();
    }
}

package com.kanva.dto.task;

import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskStatus;
import com.kanva.domain.taskseries.CompletionPolicy;
import com.kanva.domain.taskseries.TaskSeries;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TaskResponse {

    private Long id;
    private Long dailyNoteId;
    private Long seriesId;
    private String title;
    private String description;
    private TaskStatus status;
    private Integer position;
    private boolean overdue;
    private boolean repeatDaily;
    private boolean stopOnComplete;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 반복 Task 정보
    private boolean repeatDaily;
    private boolean stopOnComplete;
    private LocalDate endDate;

    public static TaskResponse from(Task task) {
        TaskSeries series = task.getSeries();
        boolean isRepeatDaily = series != null;
        boolean isStopOnComplete = series != null
                && series.getCompletionPolicy() == CompletionPolicy.COMPLETE_STOPS_SERIES;
        LocalDate endDate = series != null ? series.getEndDate() : null;

        return TaskResponse.builder()
                .id(task.getId())
                .dailyNoteId(task.getDailyNote().getId())
                .seriesId(task.getSeriesId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .position(task.getPosition())
                .overdue(task.isOverdue())
                .repeatDaily(repeatDaily)
                .stopOnComplete(stopOnComplete)
                .endDate(endDate)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .repeatDaily(isRepeatDaily)
                .stopOnComplete(isStopOnComplete)
                .endDate(endDate)
                .build();
    }
}

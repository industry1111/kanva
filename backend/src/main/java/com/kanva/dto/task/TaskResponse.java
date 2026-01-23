package com.kanva.dto.task;

import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskStatus;
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
    private LocalDate dueDate;
    private TaskStatus status;
    private Integer position;
    private boolean overdue;
    private boolean repeatDaily;
    private boolean stopOnComplete;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse from(Task task) {
        boolean repeatDaily = task.isSeriesTask();
        boolean stopOnComplete = false;
        LocalDate endDate = null;

        if (task.getSeries() != null) {
            stopOnComplete = task.getSeries().isStopOnComplete();
            endDate = task.getSeries().getEndDate();
        }

        return TaskResponse.builder()
                .id(task.getId())
                .dailyNoteId(task.getDailyNote().getId())
                .seriesId(task.getSeriesId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .position(task.getPosition())
                .overdue(task.isOverdue())
                .repeatDaily(repeatDaily)
                .stopOnComplete(stopOnComplete)
                .endDate(endDate)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}

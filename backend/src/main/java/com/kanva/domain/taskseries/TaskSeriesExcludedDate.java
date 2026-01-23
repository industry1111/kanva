package com.kanva.domain.taskseries;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_series_excluded_date")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskSeriesExcludedDate implements Persistable<TaskSeriesExcludedDateId> {

    @EmbeddedId
    private TaskSeriesExcludedDateId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskSeriesId")
    @JoinColumn(name = "task_series_id", nullable = false)
    private TaskSeries taskSeries;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private boolean isNew = true;

    public TaskSeriesExcludedDate(TaskSeries taskSeries, LocalDate date) {
        this.id = new TaskSeriesExcludedDateId(taskSeries.getId(), date);
        this.taskSeries = taskSeries;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    public LocalDate getDate() {
        return this.id.getDate();
    }
}

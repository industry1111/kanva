package com.kanva.domain.task;

import com.kanva.domain.BaseEntity;
import com.kanva.domain.dailynote.DailyNote;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tasks",
        indexes = @Index(name = "idx_daily_note_position", columnList = "daily_note_id, position")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_note_id", nullable = false)
    private DailyNote dailyNote;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Column(nullable = false)
    private Integer position;

    @Builder
    public Task(DailyNote dailyNote, String title, String description, LocalDate dueDate, TaskStatus status, Integer position) {
        this.dailyNote = dailyNote;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status != null ? status : TaskStatus.PENDING;
        this.position = position != null ? position : 0;
    }

    // 상태 변경 메서드
    public void updateStatus(TaskStatus status) {
        this.status = status;
    }

    public void complete() {
        this.status = TaskStatus.COMPLETED;
    }

    public void start() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void pending() {
        this.status = TaskStatus.PENDING;
    }

    // 기본 정보 수정 메서드
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void updatePosition(Integer position) {
        this.position = position;
    }

    // DailyNote 연관관계 설정
    public void assignToDailyNote(DailyNote dailyNote) {
        this.dailyNote = dailyNote;
    }

    /**
     * Task 완료 상태 토글
     * COMPLETED -> PENDING, 그 외 -> COMPLETED
     */
    public void toggle() {
        if (this.status == TaskStatus.COMPLETED) {
            this.status = TaskStatus.PENDING;
        } else {
            this.status = TaskStatus.COMPLETED;
        }
    }

    // 편의 메서드
    public boolean isCompleted() {
        return this.status == TaskStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return this.status == TaskStatus.IN_PROGRESS;
    }

    public boolean isPending() {
        return this.status == TaskStatus.PENDING;
    }

    public boolean isOverdue() {
        return this.dueDate != null
                && LocalDate.now().isAfter(this.dueDate)
                && !isCompleted();
    }
}

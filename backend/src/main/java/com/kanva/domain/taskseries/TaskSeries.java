package com.kanva.domain.taskseries;

import com.kanva.domain.BaseEntity;
import com.kanva.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "task_series",
        indexes = {
                @Index(name = "idx_task_series_user_status", columnList = "user_id, status"),
                @Index(name = "idx_task_series_end_date", columnList = "end_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskSeries extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskSeriesStatus status;

    @Column(name = "stop_date")
    private LocalDate stopDate;

    @Builder
    public TaskSeries(User user, String title, String description, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.endDate = endDate;
        this.status = TaskSeriesStatus.ACTIVE;
        this.stopDate = null;
    }

    /**
     * 시리즈 중단 (인스턴스 완료 시 호출)
     */
    public void stop(LocalDate stopDate) {
        this.status = TaskSeriesStatus.STOPPED;
        this.stopDate = stopDate;
    }

    /**
     * 오늘 날짜가 생성 가능 범위인지 확인
     */
    public boolean canGenerateFor(LocalDate date) {
        if (status != TaskSeriesStatus.ACTIVE) {
            return false;
        }

        // date가 startDate ~ endDate 범위 내인지 확인
        boolean inRange = !date.isBefore(startDate) && !date.isAfter(endDate);
        if (!inRange) {
            return false;
        }

        // stopDate가 설정되어 있으면 date <= stopDate인 경우에만 생성
        if (stopDate != null && date.isAfter(stopDate)) {
            return false;
        }

        return true;
    }

    public boolean isActive() {
        return this.status == TaskSeriesStatus.ACTIVE;
    }
}

package com.kanva.domain.taskseries;

import com.kanva.domain.BaseEntity;
import com.kanva.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 반복 Task 시리즈 (Master)
 *
 * - startDate ~ endDate 범위 내에서 매일 Task 인스턴스 생성
 * - completionPolicy에 따라 완료 시 동작 결정:
 *   - PER_OCCURRENCE: 해당 날짜만 완료, 시리즈 계속
 *   - COMPLETE_STOPS_SERIES: 완료 시 stopDate 설정, 이후 생성 중단
 */
@Entity
@Table(name = "task_series",
        indexes = {
                @Index(name = "idx_task_series_user", columnList = "user_id"),
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

    /**
     * 완료 정책
     * - PER_OCCURRENCE: 인스턴스별 완료 (습관/알고리즘)
     * - COMPLETE_STOPS_SERIES: 완료 시 시리즈 중단 (프로젝트/마일스톤)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "completion_policy", nullable = false, length = 30)
    private CompletionPolicy completionPolicy;

    /**
     * 시리즈 중단 날짜 (COMPLETE_STOPS_SERIES에서만 사용)
     * - null이면 endDate까지 생성
     * - 값이 있으면 date <= stopDate 인 경우에만 생성
     */
    @Column(name = "stop_date")
    private LocalDate stopDate;

    @Builder
    public TaskSeries(User user, String title, String description,
                      LocalDate startDate, LocalDate endDate,
                      CompletionPolicy completionPolicy) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completionPolicy = completionPolicy != null ? completionPolicy : CompletionPolicy.PER_OCCURRENCE;
        this.stopDate = null;
    }

    /**
     * 시리즈 중단 (COMPLETE_STOPS_SERIES 정책에서 인스턴스 완료 시 호출)
     *
     * @param taskDate 완료된 Task의 날짜
     * @return stopDate가 변경되었으면 true
     */
    public boolean stop(LocalDate taskDate) {
        if (this.completionPolicy != CompletionPolicy.COMPLETE_STOPS_SERIES) {
            return false;
        }

        // stopDate가 null이면 taskDate로 설정
        if (this.stopDate == null) {
            this.stopDate = taskDate;
            return true;
        }

        // 이미 stopDate가 있고, taskDate가 더 이르면 앞당김
        if (taskDate.isBefore(this.stopDate)) {
            this.stopDate = taskDate;
            return true;
        }

        return false;
    }

    /**
     * 특정 날짜에 Task 인스턴스 생성 가능 여부 확인
     *
     * 조건:
     * 1. startDate <= date <= endDate
     * 2. COMPLETE_STOPS_SERIES인 경우: stopDate == null OR date <= stopDate
     */
    public boolean canGenerateFor(LocalDate date) {
        // 기본 범위 체크: startDate <= date <= endDate
        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            return false;
        }

        // COMPLETE_STOPS_SERIES인 경우에만 stopDate 체크
        if (completionPolicy == CompletionPolicy.COMPLETE_STOPS_SERIES) {
            if (stopDate != null && date.isAfter(stopDate)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 시리즈가 중단되었는지 확인
     */
    public boolean isStopped() {
        return this.completionPolicy == CompletionPolicy.COMPLETE_STOPS_SERIES
                && this.stopDate != null;
    }

    /**
     * 시리즈가 활성 상태인지 확인 (stopDate 기준)
     */
    public boolean isActive() {
        return !isStopped();
    }
}

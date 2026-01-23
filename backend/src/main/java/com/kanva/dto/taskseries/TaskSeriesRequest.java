package com.kanva.dto.taskseries;

import com.kanva.domain.taskseries.CompletionPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * TaskSeries 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
public class TaskSeriesRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    private String description;

    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    /**
     * 완료 시 시리즈 중단 여부
     * - false: PER_OCCURRENCE (인스턴스별 완료)
     * - true: COMPLETE_STOPS_SERIES (완료 시 시리즈 중단)
     */
    private Boolean stopOnComplete;

    @Builder
    public TaskSeriesRequest(String title, String description, LocalDate startDate,
                             LocalDate endDate, Boolean stopOnComplete) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.stopOnComplete = stopOnComplete;
    }

    /**
     * CompletionPolicy 반환
     */
    public CompletionPolicy getCompletionPolicy() {
        return Boolean.TRUE.equals(stopOnComplete)
                ? CompletionPolicy.COMPLETE_STOPS_SERIES
                : CompletionPolicy.PER_OCCURRENCE;
    }
}

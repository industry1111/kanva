package com.kanva.dto.task;

import com.kanva.domain.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Task 생성/수정 요청 DTO
 *
 * 반복 Task 설정:
 * - repeatDaily = false: 단일 Task (시리즈 없음)
 * - repeatDaily = true: 반복 Task (시리즈 생성)
 *   - stopOnComplete = false: 인스턴스별 완료 (습관/알고리즘)
 *   - stopOnComplete = true: 완료 시 시리즈 중단 (프로젝트/마일스톤)
 */
@Getter
@NoArgsConstructor
public class TaskRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다")
    private String title;

    private String description;

    private TaskStatus status;

    private Integer position;

    /**
     * 반복 여부 (매일 자동 생성)
     * - false: 단일 Task
     * - true: 반복 Task (시리즈 생성)
     */
    private Boolean repeatDaily;

    /**
     * 완료 시 반복 중단 여부 (repeatDaily=true일 때만 유효)
     * - false: 인스턴스별 완료, 시리즈 계속 (PER_OCCURRENCE)
     * - true: 완료 시 시리즈 중단 (COMPLETE_STOPS_SERIES)
     */
    private Boolean stopOnComplete;

    /**
     * 반복 종료일 (repeatDaily=true일 때 시리즈의 endDate)
     * 단일 Task에서는 사용하지 않음
     */
    private LocalDate endDate;

    @Builder
    public TaskRequest(String title, String description, TaskStatus status, Integer position,
                       Boolean repeatDaily, Boolean stopOnComplete, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.position = position;
        this.repeatDaily = repeatDaily;
        this.stopOnComplete = stopOnComplete;
        this.endDate = endDate;
    }

    /**
     * 반복 Task인지 확인
     */
    public boolean isRepeatDaily() {
        return Boolean.TRUE.equals(repeatDaily);
    }

    /**
     * 완료 시 중단 정책인지 확인
     */
    public boolean isStopOnComplete() {
        return Boolean.TRUE.equals(stopOnComplete);
    }
}

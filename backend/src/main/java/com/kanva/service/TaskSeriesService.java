package com.kanva.service;

import com.kanva.domain.task.Task;
import com.kanva.domain.taskseries.CompletionPolicy;
import com.kanva.domain.taskseries.TaskSeries;
import com.kanva.dto.taskseries.TaskSeriesRequest;
import com.kanva.dto.taskseries.TaskSeriesResponse;

import java.time.LocalDate;
import java.util.List;

public interface TaskSeriesService {

    /**
     * 시리즈 생성 (API 직접 호출용)
     */
    TaskSeriesResponse createSeries(Long userId, TaskSeriesRequest request);

    /**
     * 기존 Task로부터 시리즈 생성 (Task 업데이트 시 repeatDaily=true인 경우)
     *
     * @param task 연결할 Task
     * @param endDate 시리즈 종료일
     * @param completionPolicy 완료 정책
     * @return 생성된 TaskSeries
     */
    TaskSeries createSeriesFromTask(Task task, LocalDate endDate, CompletionPolicy completionPolicy);

    /**
     * 사용자의 전체 시리즈 목록 조회
     */
    List<TaskSeriesResponse> getUserSeries(Long userId);

    /**
     * 사용자의 활성 시리즈 목록 조회
     */
    List<TaskSeriesResponse> getUserActiveSeries(Long userId);

    /**
     * 특정 날짜에 대한 시리즈 Task 생성 (온디맨드)
     * 사용자가 날짜를 조회할 때 호출됨
     * 미래 날짜도 생성 허용
     */
    void generateTasksForDate(Long userId, LocalDate date);

    /**
     * 스케줄러용: 오늘 날짜의 모든 사용자 시리즈 Task 생성 (fallback)
     */
    void generateTodayTasks();

    /**
     * Task 완료 시 시리즈 처리
     * - COMPLETE_STOPS_SERIES: stopDate 설정 + 미래 인스턴스 삭제
     * - PER_OCCURRENCE: 아무 작업 없음
     *
     * @param task 완료된 Task
     * @return 미래 인스턴스 삭제 개수
     */
    int handleTaskCompletion(Task task);

    /**
     * 시리즈에서 특정 날짜 제외 (해당 날짜 인스턴스 삭제 + excluded_date 기록)
     */
    void excludeDate(Long seriesId, LocalDate date);

    /**
     * 시리즈 중단 (stopDate 설정 + 이후 인스턴스 삭제)
     */
    void stopSeries(Long seriesId, LocalDate stopDate);

    /**
     * 시리즈 자동 정리 조건 확인 후 삭제
     *
     * 조건 (ALL):
     * 1) 종료 의사 확정: stopDate != null OR 전체 기간이 exclusion
     * 2) 미래 Task 생성 불가
     * 3) 해당 시리즈에 속한 Task가 0개
     *
     * @return 정리되었으면 true
     */
    boolean cleanupIfEligible(Long seriesId);
}
